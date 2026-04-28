package com.marketplace.analytics.dto;

import com.marketplace.analytics.model.TipoEventoMetrica;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Propósito: confirmación de persistencia de un evento de métrica.
 * Patrón: DTO de salida.
 * Responsabilidad: devolver identificador y datos espejo del registro creado.
 */
public record EventoMetricaResponse(
        Long id,
        TipoEventoMetrica tipo,
        String referencia,
        BigDecimal valorMonetario,
        Instant ocurridoEn
) {
}
