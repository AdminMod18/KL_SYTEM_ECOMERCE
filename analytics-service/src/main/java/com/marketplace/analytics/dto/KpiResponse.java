package com.marketplace.analytics.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Propósito: vista agregada de indicadores clave del marketplace.
 * Patrón: DTO de salida / read model.
 * Responsabilidad: exponer conteos y sumas derivadas de {@link com.marketplace.analytics.entity.EventoMetrica}.
 */
public record KpiResponse(
        long totalEventos,
        long comprasRegistradas,
        BigDecimal ingresosComprasAcumulados,
        long solicitudesAprobadasRegistradas,
        Instant ultimoEventoEn
) {
}
