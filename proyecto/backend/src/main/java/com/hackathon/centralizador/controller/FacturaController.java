package com.hackathon.centralizador.controller;

import com.hackathon.centralizador.dto.FacturaRequest;
import com.hackathon.centralizador.dto.FacturaResponse;
import com.hackathon.centralizador.service.FacturaService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
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
     * Lista facturas de forma paginada con ordenamiento por defecto por fechaCreacion descendente.
     *
     * @param pageable parámetros de paginación (page, size, sort)
     * @return 200 con Page de facturas
     */
    @GetMapping
    public ResponseEntity<Page<FacturaResponse>> listarFacturas(
            @PageableDefault(size = 20, sort = "fechaCreacion",
                    direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<FacturaResponse> page = facturaService.listar(pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Retorna un resumen con el conteo de facturas por cada estado.
     * Incluye todos los estados aunque tengan conteo 0.
     *
     * @return 200 con mapa {PENDIENTE: N, PROCESO: N, TERMINADO: N, ERROR: N}
     */
    @GetMapping("/estados/resumen")
    public ResponseEntity<Map<String, Long>> resumenEstados() {
        Map<String, Long> resumen = facturaService.resumenEstados();
        return ResponseEntity.ok(resumen);
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
