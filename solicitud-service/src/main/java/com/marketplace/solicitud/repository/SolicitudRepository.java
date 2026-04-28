package com.marketplace.solicitud.repository;

import com.marketplace.solicitud.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Propósito: acceso a datos de solicitudes (persistencia JPA).
 * Patrón: Repository (DDD / Spring Data).
 * Responsabilidad: consultas CRUD y apoyo a reglas de negocio (duplicados, etc.).
 */
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    boolean existsByDocumentoIdentidad(String documentoIdentidad);

    Optional<Solicitud> findByDocumentoIdentidad(String documentoIdentidad);
}
