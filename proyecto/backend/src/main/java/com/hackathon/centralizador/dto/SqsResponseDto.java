package com.hackathon.centralizador.dto;

/**
 * DTO que representa el mensaje recibido desde la cola SQS de respuesta (facturas-respuesta).
 * Contiene el resultado de la contabilización realizada por la Lambda_Contabilizadora.
 *
 * @param facturaId    identificador de la factura procesada
 * @param status       resultado del procesamiento: "TERMINADO" o "ERROR"
 * @param errorMessage descripción del error, presente solo cuando status es "ERROR"
 */
public record SqsResponseDto(
        Long facturaId,
        String status,
        String errorMessage
) {}
