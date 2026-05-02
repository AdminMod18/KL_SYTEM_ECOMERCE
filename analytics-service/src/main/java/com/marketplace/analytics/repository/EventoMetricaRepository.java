package com.marketplace.analytics.repository;

import com.marketplace.analytics.entity.EventoMetrica;
import com.marketplace.analytics.model.TipoEventoMetrica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Propósito: acceso a datos de eventos y consultas agregadas para KPIs.
 * Patrón: Repository (Spring Data JPA).
 * Responsabilidad: exponer conteos y sumas por tipo de evento.
 */
public interface EventoMetricaRepository extends JpaRepository<EventoMetrica, Long> {

    long countByTipo(TipoEventoMetrica tipo);

    @Query("select coalesce(sum(e.valorMonetario), 0) from EventoMetrica e where e.tipo = :tipo")
    BigDecimal sumValorMonetarioByTipo(TipoEventoMetrica tipo);

    long count();

    @Query("select max(e.ocurridoEn) from EventoMetrica e")
    Instant findMaxOcurridoEn();

    @Query(
            value =
                    "SELECT referencia FROM eventos_metrica WHERE tipo = :tipo GROUP BY referencia ORDER BY COUNT(*) DESC LIMIT 1",
            nativeQuery = true)
    String findReferenciaMasFrecuentePorTipo(@Param("tipo") String tipo);
}
