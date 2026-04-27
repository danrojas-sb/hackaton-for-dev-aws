package com.hackathon.centralizador.repository;

import com.hackathon.centralizador.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para operaciones CRUD sobre la entidad Factura.
 */
public interface FacturaRepository extends JpaRepository<Factura, Long> {
}
