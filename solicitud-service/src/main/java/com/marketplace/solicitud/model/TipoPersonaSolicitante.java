package com.marketplace.solicitud.model;

/**
 * Propósito: clasificar al solicitante según el registro mercantil/tributario esperado.
 * Patrón: Type-safe enum (catálogo cerrado de dominio).
 * Responsabilidad: discriminar documentación obligatoria en la cadena de validación.
 */
public enum TipoPersonaSolicitante {
    NATURAL,
    JURIDICA
}
