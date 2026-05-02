package com.marketplace.solicitud.repository;

import com.marketplace.solicitud.entity.Solicitud;
import com.marketplace.solicitud.model.SolicitudEstado;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Propósito: acceso a datos de solicitudes (persistencia JPA).
 * Patrón: Repository (DDD / Spring Data).
 * Responsabilidad: consultas CRUD y apoyo a reglas de negocio (duplicados, etc.).
 */
public interface SolicitudRepository extends JpaRepository<Solicitud, Long>, JpaSpecificationExecutor<Solicitud> {

    boolean existsByDocumentoIdentidad(String documentoIdentidad);

    Optional<Solicitud> findByDocumentoIdentidad(String documentoIdentidad);

    @EntityGraph(attributePaths = {"adjuntos"})
    @Query("select s from Solicitud s where s.id = :id")
    Optional<Solicitud> findWithAdjuntosById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"adjuntos"})
    @Query("select s from Solicitud s")
    List<Solicitud> findAllWithAdjuntos();

    List<Solicitud> findByEstadoAndProximoVencimientoSuscripcionIsNotNullAndProximoVencimientoSuscripcionBefore(
            SolicitudEstado estado, Instant antesDe);

    List<Solicitud> findByEstadoAndProximoVencimientoSuscripcionIsNotNull(SolicitudEstado estado);

    List<Solicitud> findByEstadoIn(Collection<SolicitudEstado> estados);
}
