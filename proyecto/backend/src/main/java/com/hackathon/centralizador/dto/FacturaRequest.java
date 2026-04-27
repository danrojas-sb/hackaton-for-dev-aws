package com.hackathon.centralizador.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de entrada para la creación de facturas en lote.
 * Aplica validaciones Jakarta sobre los campos obligatorios y opcionales.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaRequest {

    @Size(max = 20, message = "cus no debe exceder 20 caracteres")
    private String cus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaPago;

    @Size(max = 100, message = "responsabilidadFiscal no debe exceder 100 caracteres")
    private String responsabilidadFiscal;

    @NotBlank(message = "tipoDocumento es obligatorio")
    @Size(max = 10, message = "tipoDocumento no debe exceder 10 caracteres")
    private String tipoDocumento;

    @NotBlank(message = "numeroDocumento es obligatorio")
    @Size(max = 20, message = "numeroDocumento no debe exceder 20 caracteres")
    private String numeroDocumento;

    @NotBlank(message = "nombres es obligatorio")
    @Size(max = 100, message = "nombres no debe exceder 100 caracteres")
    private String nombres;

    @Size(max = 100, message = "apellidos no debe exceder 100 caracteres")
    private String apellidos;

    @Size(max = 20, message = "telefono no debe exceder 20 caracteres")
    private String telefono;

    @Size(max = 100, message = "municipio no debe exceder 100 caracteres")
    private String municipio;

    @Size(max = 255, message = "direccion no debe exceder 255 caracteres")
    private String direccion;

    @Email(message = "email debe tener un formato válido")
    @Size(max = 150, message = "email no debe exceder 150 caracteres")
    private String email;

    @NotNull(message = "valorPack es obligatorio")
    @Positive(message = "valorPack debe ser positivo")
    private BigDecimal valorPack;

    @Positive(message = "valorPackIva debe ser positivo")
    private BigDecimal valorPackIva;

    private String comentarios;
}
