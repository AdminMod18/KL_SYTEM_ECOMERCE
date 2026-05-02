package com.marketplace.solicitud.repository;

import com.marketplace.solicitud.entity.CalificacionVendedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CalificacionVendedorRepository extends JpaRepository<CalificacionVendedor, Long> {

    long countBySolicitud_Id(Long solicitudId);

    long countBySolicitud_IdAndValorLessThan(Long solicitudId, int valorExclusiveUpperBound);

    @Query("select avg(c.valor) from CalificacionVendedor c where c.solicitud.id = :sid")
    Optional<Double> promedioValorPorSolicitud(@Param("sid") Long solicitudId);
}
