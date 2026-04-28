package com.marketplace.validation.domain;

/**
 * Propósito: vista neutral del resultado de consulta crediticia (dominio).
 * Patrón: Value Object / DTO de dominio.
 * Responsabilidad: transportar score y banderas usadas por la fachada sin JSON de proveedor.
 */
public record ResultadoDatacredito(
        int score,
        boolean listaControl,
        String referenciaConsulta
) {
}
