package com.hackathon.centralizador.exception;

/**
 * Excepción lanzada cuando no se encuentra una factura con el identificador proporcionado.
 * Se utiliza en operaciones que requieren una factura existente, como el reintento
 * o la actualización de estado desde la respuesta SQS.
 */
public class FacturaNotFoundException extends RuntimeException {

    /**
     * Crea una nueva excepción indicando el ID de la factura no encontrada.
     *
     * @param facturaId identificador de la factura que no fue encontrada
     */
    public FacturaNotFoundException(Long facturaId) {
        super("Factura no encontrada con id: " + facturaId);
    }
}
