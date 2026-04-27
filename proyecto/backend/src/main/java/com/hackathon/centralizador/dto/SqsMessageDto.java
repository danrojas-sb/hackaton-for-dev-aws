package com.hackathon.centralizador.dto;

import java.math.BigDecimal;

/**
 * DTO que representa el mensaje enviado a la cola SQS de envío (facturas-envio).
 * Contiene todos los datos de la factura necesarios para la contabilización
 * por parte de la Lambda_Contabilizadora.
 *
 * @param facturaId            identificador único de la factura en el Centralizador
 * @param cus                  código único de servicio
 * @param fechaPago            fecha de pago en formato ISO-8601 (yyyy-MM-dd)
 * @param responsabilidadFiscal responsabilidad fiscal del titular
 * @param tipoDocumento        tipo de documento de identidad
 * @param numeroDocumento      número de documento de identidad
 * @param nombres              nombres del titular
 * @param apellidos            apellidos del titular
 * @param telefono             teléfono de contacto
 * @param municipio            municipio de residencia
 * @param direccion            dirección de residencia
 * @param email                correo electrónico de contacto
 * @param valorPack            valor del pack sin IVA
 * @param valorPackIva         valor del pack con IVA
 * @param comentarios          comentarios adicionales
 */
public record SqsMessageDto(
        Long facturaId,
        String cus,
        String fechaPago,
        String responsabilidadFiscal,
        String tipoDocumento,
        String numeroDocumento,
        String nombres,
        String apellidos,
        String telefono,
        String municipio,
        String direccion,
        String email,
        BigDecimal valorPack,
        BigDecimal valorPackIva,
        String comentarios
) {}
