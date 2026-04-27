package com.hackathon.centralizador.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA mapeada a la tabla centralizador.facturas.
 * Contiene los 14 campos del CSV más estado y timestamps de auditoría.
 */
@Entity
@Table(name = "facturas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cus", length = 20)
    private String cus;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Column(name = "responsabilidad_fiscal", length = 100)
    private String responsabilidadFiscal;

    @Column(name = "tipo_documento", nullable = false, length = 10)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", length = 100)
    private String apellidos;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "municipio", length = 100)
    private String municipio;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "valor_pack", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorPack;

    @Column(name = "valor_pack_iva", precision = 12, scale = 2)
    private BigDecimal valorPackIva;

    @Column(name = "comentarios", columnDefinition = "TEXT")
    private String comentarios;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoFactura estado;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
}
