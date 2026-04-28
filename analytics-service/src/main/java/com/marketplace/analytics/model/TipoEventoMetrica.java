package com.marketplace.analytics.model;

/**
 * Propósito: tipos de eventos que alimentan los KPIs del catálogo analítico.
 * Patrón: enumeración de dominio.
 * Responsabilidad: clasificar hechos de negocio persistidos para agregaciones.
 */
public enum TipoEventoMetrica {
    COMPRA,
    SOLICITUD_APROBADA
}
