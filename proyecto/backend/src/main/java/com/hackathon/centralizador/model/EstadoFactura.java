package com.hackathon.centralizador.model;

/**
 * Estados posibles de una factura en el flujo de procesamiento.
 */
public enum EstadoFactura {
    PENDIENTE,
    PROCESO,
    TERMINADO,
    ERROR
}
