package com.hackathon.centralizador.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.centralizador.dto.FacturaRequest;
import com.hackathon.centralizador.dto.FacturaResponse;
import com.hackathon.centralizador.service.FacturaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración para FacturaController con MockMvc.
 */
@WebMvcTest(FacturaController.class)
class FacturaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FacturaService facturaService;

    @Test
    @DisplayName("Given JSON válido, When POST /api/facturas/bulk, Then 200 con facturas")
    void givenValidJson_whenPostBulk_thenReturns200WithFacturas() throws Exception {
        // Given
        FacturaRequest request = FacturaRequest.builder()
                .tipoDocumento("CC")
                .numeroDocumento("123456")
                .nombres("Juan")
                .valorPack(new BigDecimal("50000.00"))
                .build();

        FacturaResponse response = FacturaResponse.builder()
                .id(1L)
                .tipoDocumento("CC")
                .numeroDocumento("123456")
                .nombres("Juan")
                .valorPack(new BigDecimal("50000.00"))
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.of(2025, 1, 15, 10, 30, 0))
                .build();

        when(facturaService.crearFacturasEnLote(anyList())).thenReturn(List.of(response));

        // When & Then
        mockMvc.perform(post("/api/facturas/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].tipoDocumento").value("CC"))
                .andExpect(jsonPath("$[0].numeroDocumento").value("123456"))
                .andExpect(jsonPath("$[0].nombres").value("Juan"))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$[0].fechaCreacion").exists());
    }

    @Test
    @DisplayName("Given JSON con campos faltantes, When POST, Then 400 con errores")
    void givenInvalidJson_whenPostBulk_thenReturns400WithErrors() throws Exception {
        // Given - request sin campos obligatorios
        String invalidJson = "[{\"cus\": \"123\"}]";

        // When & Then
        mockMvc.perform(post("/api/facturas/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Given facturas en BD, When GET /api/facturas, Then 200 con Page")
    void givenFacturasEnBd_whenGetFacturas_thenReturns200WithPage() throws Exception {
        // Given
        FacturaResponse response = FacturaResponse.builder()
                .id(1L)
                .tipoDocumento("CC")
                .numeroDocumento("123456")
                .nombres("Juan")
                .valorPack(new BigDecimal("50000.00"))
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.of(2025, 1, 15, 10, 30, 0))
                .build();

        Page<FacturaResponse> page = new PageImpl<>(
                List.of(response), PageRequest.of(0, 20), 1);

        when(facturaService.listar(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/facturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].tipoDocumento").value("CC"))
                .andExpect(jsonPath("$.content[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("Given facturas con distintos estados, When GET /api/facturas/estados/resumen, Then conteo correcto")
    void givenFacturasConDistintosEstados_whenGetResumen_thenConteosCorrecto() throws Exception {
        // Given
        Map<String, Long> resumen = new LinkedHashMap<>();
        resumen.put("PENDIENTE", 5L);
        resumen.put("PROCESO", 3L);
        resumen.put("TERMINADO", 2L);
        resumen.put("ERROR", 1L);

        when(facturaService.resumenEstados()).thenReturn(resumen);

        // When & Then
        mockMvc.perform(get("/api/facturas/estados/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PENDIENTE").value(5))
                .andExpect(jsonPath("$.PROCESO").value(3))
                .andExpect(jsonPath("$.TERMINADO").value(2))
                .andExpect(jsonPath("$.ERROR").value(1));
    }

    @Test
    @DisplayName("Given sin facturas, When GET /api/facturas/estados/resumen, Then todos los estados en 0")
    void givenSinFacturas_whenGetResumen_thenTodosEstadosEnCero() throws Exception {
        // Given
        Map<String, Long> resumen = new LinkedHashMap<>();
        resumen.put("PENDIENTE", 0L);
        resumen.put("PROCESO", 0L);
        resumen.put("TERMINADO", 0L);
        resumen.put("ERROR", 0L);

        when(facturaService.resumenEstados()).thenReturn(resumen);

        // When & Then
        mockMvc.perform(get("/api/facturas/estados/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PENDIENTE").value(0))
                .andExpect(jsonPath("$.PROCESO").value(0))
                .andExpect(jsonPath("$.TERMINADO").value(0))
                .andExpect(jsonPath("$.ERROR").value(0));
    }
}
