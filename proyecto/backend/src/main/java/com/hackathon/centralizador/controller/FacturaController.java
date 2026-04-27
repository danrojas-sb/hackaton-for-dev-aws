package com.hackathon.centralizador.controller;

import com.hackathon.centralizador.dto.FacturaRequest;
import com.hackathon.centralizador.dto.FacturaResponse;
import com.hackathon.centralizador.service.FacturaService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para operaciones masivas sobre facturas.
 */
@RestController
@RequestMapping("/api/facturas")
@Validated
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    /**
     * Recibe un lote de facturas y las persiste con estado PENDIENTE.
     *
     * @param requests lista de facturas a crear
     * @return 200 con la lista de facturas creadas
     */
    @PostMapping("/bulk")
    public ResponseEntity<List<FacturaResponse>> crearFacturasEnLote(
            @RequestBody List<@Valid FacturaRequest> requests) {
        List<FacturaResponse> responses = facturaService.crearFacturasEnLote(requests);
        return ResponseEntity.ok(responses);
    }

    /**
     * Maneja errores de validación Jakarta y retorna 400 con detalle de campos.
     *
     * @param ex excepción de validación
     * @return mapa con campo y mensaje de error
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = (error instanceof FieldError fieldError)
                    ? fieldError.getField()
                    : error.getObjectName();
            errors.put(fieldName, error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Maneja errores de validación de constraints en listas y retorna 400.
     *
     * @param ex excepción de constraint violation
     * @return mapa con campo y mensaje de error
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String path = violation.getPropertyPath().toString();
            errors.put(path, violation.getMessage());
        }
        return ResponseEntity.badRequest().body(errors);
    }
}
