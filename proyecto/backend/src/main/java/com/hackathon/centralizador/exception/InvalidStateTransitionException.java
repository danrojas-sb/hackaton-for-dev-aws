package com.hackathon.centralizador.exception;

import com.hackathon.centralizador.model.EstadoFactura;

/**
 * Excepción lanzada cuando se intenta una transición de estado no permitida
 * en el ciclo de vida de una factura.
 * Las transiciones válidas son: PENDIENTE→PROCESO, PROCESO→TERMINADO,
 * PROCESO→ERROR, ERROR→PENDIENTE.
 */
public class InvalidStateTransitionException extends RuntimeException {

    /**
     * Crea una nueva excepción indicando la transición no permitida.
     *
     * @param estadoActual estado actual de la factura
     * @param estadoDestino estado destino que no está permitido desde el estado actual
     */
    public InvalidStateTransitionException(EstadoFactura estadoActual, EstadoFactura estadoDestino) {
        super("Transición de estado no permitida: " + estadoActual + " → " + estadoDestino);
    }
}
