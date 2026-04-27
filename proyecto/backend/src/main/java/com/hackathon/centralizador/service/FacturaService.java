package com.hackathon.centralizador.service;

import com.hackathon.centralizador.dto.FacturaRequest;
import com.hackathon.centralizador.dto.FacturaResponse;
import com.hackathon.centralizador.model.EstadoFactura;
import com.hackathon.centralizador.model.Factura;
import com.hackathon.centralizador.repository.FacturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
