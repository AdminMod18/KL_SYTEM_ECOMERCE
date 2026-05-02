package com.marketplace.solicitud.model;

/**
 * Propósito: tipos de anexos exigidos en el proceso de alta de vendedor.
 * Patrón: Type-safe enum (catálogo cerrado).
 * Responsabilidad: insumo para {@link com.marketplace.solicitud.chain.DocumentosPorTipoPersonaHandler}.
 */
public enum TipoDocumentoAdjunto {
    CEDULA,
    RUT,
    CAMARA_COMERCIO,
    ACEPTACION_CENTRALES_RIESGO,
    ACEPTACION_DATOS_PERSONALES
}
