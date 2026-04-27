package com.hackathon.centralizador.service;

import com.hackathon.centralizador.dto.FacturaRequest;
import com.hackathon.centralizador.dto.FacturaResponse;
import com.hackathon.centralizador.model.EstadoFactura;
import com.hackathon.centralizador.model.Factura;
import com.hackathon.centralizador.repository.FacturaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para FacturaService con repositorio mockeado.
 */
@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @InjectMocks
    private FacturaService facturaService;

    @Captor
    private ArgumentCaptor<List<Factura>> facturasCaptor;

    @Test
    @DisplayName("Given lista de requests válidos, When crearFacturasEnLote, Then persiste con estado PENDIENTE")
    void givenValidRequests_whenCrearFacturasEnLote_thenPersistsWithEstadoPendiente() {
        // Given
        FacturaRequest request = FacturaRequest.builder()
                .tipoDocumento("CC")
                .numeroDocumento("123456")
                .nombres("Juan")
                .apellidos("Pérez")
                .valorPack(new BigDecimal("50000.00"))
                .build();

        Factura savedFactura = Factura.builder()
                .id(1L)
                .tipoDocumento("CC")
                .numeroDocumento("123456")
                .nombres("Juan")
                .apellidos("Pérez")
                .valorPack(new BigDecimal("50000.00"))
                .estado(EstadoFactura.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(facturaRepository.saveAll(anyList())).thenReturn(List.of(savedFactura));

        // When
        List<FacturaResponse> responses = facturaService.crearFacturasEnLote(List.of(request));

        // Then
        verify(facturaRepository).saveAll(facturasCaptor.capture());
        List<Factura> captured = facturasCaptor.getValue();

        assertThat(captured).hasSize(1);
        assertThat(captured.get(0).getEstado()).isEqualTo(EstadoFactura.PENDIENTE);
        assertThat(captured.get(0).getTipoDocumento()).isEqualTo("CC");
        assertThat(captured.get(0).getNumeroDocumento()).isEqualTo("123456");
        assertThat(captured.get(0).getNombres()).isEqualTo("Juan");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(0).getEstado()).isEqualTo("PENDIENTE");
        assertThat(responses.get(0).getTipoDocumento()).isEqualTo("CC");
    }

    @Test
    @DisplayName("Given lista vacía, When crearFacturasEnLote, Then retorna lista vacía")
    void givenEmptyList_whenCrearFacturasEnLote_thenReturnsEmptyList() {
        // Given
        when(facturaRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // When
        List<FacturaResponse> responses = facturaService.crearFacturasEnLote(Collections.emptyList());

        // Then
        assertThat(responses).isEmpty();
        verify(facturaRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Given facturas en BD, When listar con Pageable, Then retorna Page de FacturaResponse")
    void givenFacturasEnBd_whenListar_thenReturnsPage() {
        // Given
        Factura factura = Factura.builder()
                .id(1L)
                .tipoDocumento("CC")
                .numeroDocumento("123456")
                .nombres("Juan")
                .valorPack(new BigDecimal("50000.00"))
                .estado(EstadoFactura.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        Page<Factura> page = new PageImpl<>(List.of(factura), pageable, 1);

        when(facturaRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        Page<FacturaResponse> result = facturaService.listar(pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getEstado()).isEqualTo("PENDIENTE");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Given facturas con distintos estados, When resumenEstados, Then conteo correcto")
    void givenFacturasConDistintosEstados_whenResumenEstados_thenConteoCorrecto() {
        // Given
        List<Object[]> queryResult = List.of(
                new Object[]{EstadoFactura.PENDIENTE, 5L},
                new Object[]{EstadoFactura.PROCESO, 3L},
                new Object[]{EstadoFactura.TERMINADO, 2L},
                new Object[]{EstadoFactura.ERROR, 1L}
        );

        when(facturaRepository.contarPorEstado()).thenReturn(queryResult);

        // When
        Map<String, Long> resumen = facturaService.resumenEstados();

        // Then
        assertThat(resumen).containsEntry("PENDIENTE", 5L);
        assertThat(resumen).containsEntry("PROCESO", 3L);
        assertThat(resumen).containsEntry("TERMINADO", 2L);
        assertThat(resumen).containsEntry("ERROR", 1L);
    }

    @Test
    @DisplayName("Given sin facturas, When resumenEstados, Then todos los estados en 0")
    void givenSinFacturas_whenResumenEstados_thenTodosEstadosEnCero() {
        // Given
        when(facturaRepository.contarPorEstado()).thenReturn(Collections.emptyList());

        // When
        Map<String, Long> resumen = facturaService.resumenEstados();

        // Then
        assertThat(resumen).containsEntry("PENDIENTE", 0L);
        assertThat(resumen).containsEntry("PROCESO", 0L);
        assertThat(resumen).containsEntry("TERMINADO", 0L);
        assertThat(resumen).containsEntry("ERROR", 0L);
    }
}
