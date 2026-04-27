package com.hackathon.centralizador.service;

import com.hackathon.centralizador.dto.FacturaRequest;
import com.hackathon.centralizador.dto.FacturaResponse;
import com.hackathon.centralizador.exception.FacturaNotFoundException;
import com.hackathon.centralizador.exception.InvalidStateTransitionException;
import com.hackathon.centralizador.model.EstadoFactura;
import com.hackathon.centralizador.model.Factura;
import com.hackathon.centralizador.repository.FacturaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para operaciones de negocio sobre facturas.
 */
@Service
public class FacturaService {

    private static final Logger log = LoggerFactory.getLogger(FacturaService.class);

    /**
     * Transiciones de estado permitidas en el ciclo de vida de una factura.
     */
    private static final Map<EstadoFactura, Set<EstadoFactura>> TRANSICIONES_PERMITIDAS = Map.of(
            EstadoFactura.PENDIENTE, Set.of(EstadoFactura.PROCESO),
            EstadoFactura.PROCESO, Set.of(EstadoFactura.TERMINADO, EstadoFactura.ERROR),
            EstadoFactura.ERROR, Set.of(EstadoFactura.PENDIENTE)
    );

    private final FacturaRepository facturaRepository;
    private final SqsProducerService sqsProducerService;

    public FacturaService(FacturaRepository facturaRepository,
                          SqsProducerService sqsProducerService) {
        this.facturaRepository = facturaRepository;
        this.sqsProducerService = sqsProducerService;
    }

    /**
     * Crea un lote de facturas a partir de los requests recibidos.
     * Cada factura se persiste con estado PENDIENTE.
     *
     * @param requests lista de DTOs de entrada
     * @return lista de DTOs de salida con id, estado y fechaCreacion asignados
     */
    @Transactional
    public List<FacturaResponse> crearFacturasEnLote(List<FacturaRequest> requests) {
        List<Factura> facturas = requests.stream()
                .map(this::mapToEntity)
                .toList();

        List<Factura> saved = facturaRepository.saveAll(facturas);

        enviarASqs(saved);

        return saved.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Envía cada factura del lote a la cola SQS de envío.
     * Si el envío es exitoso, cambia el estado de PENDIENTE a PROCESO y persiste.
     * Si falla, mantiene el estado PENDIENTE y registra el error en el log.
     *
     * @param facturas lista de facturas a enviar
     */
    public void enviarASqs(List<Factura> facturas) {
        for (Factura factura : facturas) {
            boolean enviado = sqsProducerService.enviarMensaje(factura);
            if (enviado) {
                validarTransicion(factura.getEstado(), EstadoFactura.PROCESO);
                factura.setEstado(EstadoFactura.PROCESO);
                facturaRepository.save(factura);
            } else {
                log.error("Fallo al enviar factura id={} a SQS, estado permanece PENDIENTE", factura.getId());
            }
        }
    }

    /**
     * Actualiza el estado de una factura según la respuesta recibida de la Lambda vía SQS.
     * Valida que la transición de estado sea permitida antes de aplicarla.
     *
     * @param facturaId identificador de la factura a actualizar
     * @param status    estado recibido de la respuesta ("TERMINADO" o "ERROR")
     * @throws FacturaNotFoundException si la factura no existe
     * @throws InvalidStateTransitionException si la transición no es permitida
     */
    @Transactional
    public void actualizarEstadoDesdeRespuesta(Long facturaId, String status) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new FacturaNotFoundException(facturaId));

        EstadoFactura nuevoEstado = EstadoFactura.valueOf(status);
        validarTransicion(factura.getEstado(), nuevoEstado);
        factura.setEstado(nuevoEstado);
        facturaRepository.save(factura);
    }

    /**
     * Reintenta el procesamiento de una factura en estado ERROR.
     * Cambia el estado de ERROR a PENDIENTE, persiste y reenvía a la cola SQS.
     *
     * @param id identificador de la factura a reintentar
     * @return DTO con los datos actualizados de la factura
     * @throws FacturaNotFoundException si la factura no existe
     * @throws InvalidStateTransitionException si la factura no está en estado ERROR
     */
    @Transactional
    public FacturaResponse reintentarFactura(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new FacturaNotFoundException(id));

        validarTransicion(factura.getEstado(), EstadoFactura.PENDIENTE);
        factura.setEstado(EstadoFactura.PENDIENTE);
        facturaRepository.save(factura);

        enviarASqs(List.of(factura));

        return mapToResponse(factura);
    }

    /**
     * Lista facturas de forma paginada.
     *
     * @param pageable parámetros de paginación y ordenamiento
     * @return página de facturas mapeadas a DTO
     */
    @Transactional(readOnly = true)
    public Page<FacturaResponse> listar(Pageable pageable) {
        return facturaRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Retorna un resumen con el conteo de facturas por estado.
     * Incluye todos los estados del enum aunque tengan conteo 0.
     *
     * @return mapa con cada estado y su conteo
     */
    @Transactional(readOnly = true)
    public Map<String, Long> resumenEstados() {
        Map<String, Long> resumen = Arrays.stream(EstadoFactura.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        estado -> 0L,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        facturaRepository.contarPorEstado().forEach(row -> {
            EstadoFactura estado = (EstadoFactura) row[0];
            Long count = (Long) row[1];
            resumen.put(estado.name(), count);
        });

        return resumen;
    }

    /**
     * Valida que la transición de estado sea permitida según la máquina de estados.
     * Transiciones válidas: PENDIENTE→PROCESO, PROCESO→TERMINADO, PROCESO→ERROR, ERROR→PENDIENTE.
     *
     * @param actual estado actual de la factura
     * @param nuevo  estado destino deseado
     * @throws InvalidStateTransitionException si la transición no está permitida
     */
    private void validarTransicion(EstadoFactura actual, EstadoFactura nuevo) {
        Set<EstadoFactura> permitidos = TRANSICIONES_PERMITIDAS.getOrDefault(actual, Set.of());
        if (!permitidos.contains(nuevo)) {
            throw new InvalidStateTransitionException(actual, nuevo);
        }
    }

    private Factura mapToEntity(FacturaRequest request) {
        return Factura.builder()
                .cus(request.getCus())
                .fechaPago(request.getFechaPago())
                .responsabilidadFiscal(request.getResponsabilidadFiscal())
                .tipoDocumento(request.getTipoDocumento())
                .numeroDocumento(request.getNumeroDocumento())
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .telefono(request.getTelefono())
                .municipio(request.getMunicipio())
                .direccion(request.getDireccion())
                .email(request.getEmail())
                .valorPack(request.getValorPack())
                .valorPackIva(request.getValorPackIva())
                .comentarios(request.getComentarios())
                .estado(EstadoFactura.PENDIENTE)
                .build();
    }

    private FacturaResponse mapToResponse(Factura factura) {
        return FacturaResponse.builder()
                .id(factura.getId())
                .cus(factura.getCus())
                .fechaPago(factura.getFechaPago())
                .responsabilidadFiscal(factura.getResponsabilidadFiscal())
                .tipoDocumento(factura.getTipoDocumento())
                .numeroDocumento(factura.getNumeroDocumento())
                .nombres(factura.getNombres())
                .apellidos(factura.getApellidos())
                .telefono(factura.getTelefono())
                .municipio(factura.getMunicipio())
                .direccion(factura.getDireccion())
                .email(factura.getEmail())
                .valorPack(factura.getValorPack())
                .valorPackIva(factura.getValorPackIva())
                .comentarios(factura.getComentarios())
                .estado(factura.getEstado().name())
                .fechaCreacion(factura.getFechaCreacion())
                .build();
    }
}
