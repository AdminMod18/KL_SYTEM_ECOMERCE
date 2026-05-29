package com.marketplace.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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
        long consultasCatalogoRegistradas,
        String skuCompraMasFrecuente,
        String textoConsultaMasFrecuente,
        Instant ultimoEventoEn,
        String tendenciasMarketingResumen
) {

    @JsonProperty("totalEvents")
    public long totalEvents() {
        return totalEventos;
    }

    @JsonProperty("purchases")
    public long purchases() {
        return comprasRegistradas;
    }

    @JsonProperty("totalRevenue")
    public BigDecimal totalRevenue() {
        return ingresosComprasAcumulados;
    }

    @JsonProperty("approvedRequests")
    public long approvedRequests() {
        return solicitudesAprobadasRegistradas;
    }

    @JsonProperty("catalogViews")
    public long catalogViews() {
        return consultasCatalogoRegistradas;
    }

    @JsonProperty("topPurchaseSku")
    public String topPurchaseSku() {
        return skuCompraMasFrecuente;
    }

    @JsonProperty("topCatalogQuery")
    public String topCatalogQuery() {
        return textoConsultaMasFrecuente;
    }

    @JsonProperty("lastEventAt")
    public Instant lastEventAt() {
        return ultimoEventoEn;
    }

    @JsonProperty("marketingSummary")
    public String marketingSummary() {
        return tendenciasMarketingResumen;
    }
}
