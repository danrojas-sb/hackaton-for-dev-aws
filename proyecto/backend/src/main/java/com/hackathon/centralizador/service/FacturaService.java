package com.hackathon.centralizador.service;

import com.hackathon.centralizador.dto.FacturaRequest;
import com.hackathon.centralizador.dto.FacturaResponse;
import com.hackathon.centralizador.model.EstadoFactura;
import com.hackathon.centralizador.model.Factura;
import com.hackathon.centralizador.repository.FacturaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para operaciones de negocio sobre facturas.
 */
@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;

    public FacturaService(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
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

        return saved.stream()
                .map(this::mapToResponse)
                .toList();
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
