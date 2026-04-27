package com.hackathon.centralizador.repository;

import com.hackathon.centralizador.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repositorio JPA para operaciones CRUD sobre la entidad Factura.
 */
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    /**
     * Retorna el conteo de facturas agrupado por estado.
     *
     * @return lista de arreglos [EstadoFactura, Long]
     */
    @Query("SELECT f.estado, COUNT(f) FROM Factura f GROUP BY f.estado")
    List<Object[]> contarPorEstado();
}
