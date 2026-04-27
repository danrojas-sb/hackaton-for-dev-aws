package com.hackathon.centralizador.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de salida con todos los campos de la factura creada,
 * incluyendo id, estado y fechaCreacion asignados por el sistema.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaResponse {

    private Long id;
    private String cus;
    private LocalDate fechaPago;
    private String responsabilidadFiscal;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String municipio;
    private String direccion;
    private String email;
    private BigDecimal valorPack;
    private BigDecimal valorPackIva;
    private String comentarios;
    private String estado;
    private LocalDateTime fechaCreacion;
}
