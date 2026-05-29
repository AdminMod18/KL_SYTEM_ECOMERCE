package com.marketplace.analytics.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Propósito: tipos de eventos que alimentan los KPIs del catálogo analítico.
 * Patrón: enumeración de dominio.
 * Responsabilidad: clasificar hechos de negocio persistidos para agregaciones.
 */
public enum TipoEventoMetrica {
    COMPRA,
    SOLICITUD_APROBADA,
    /** HU-23/24: búsquedas o vistas en catálogo para tendencias. */
    CONSULTA_CATALOGO;

    @JsonCreator
    public static TipoEventoMetrica fromJson(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().replace('-', '_').replace(' ', '_').toUpperCase();
        return switch (normalized) {
            case "PURCHASE", "ORDER", "ORDEN", "VENTA" -> COMPRA;
            case "APPROVED_REQUEST", "APPROVED_SELLER", "SELLER_APPROVED", "SOLICITUD_APROBADA" -> SOLICITUD_APROBADA;
            case "CATALOG_VIEW", "CATALOG_SEARCH", "PRODUCT_VIEW", "CONSULTA_CATALOGO" -> CONSULTA_CATALOGO;
            default -> TipoEventoMetrica.valueOf(normalized);
        };
    }
}
