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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
}
