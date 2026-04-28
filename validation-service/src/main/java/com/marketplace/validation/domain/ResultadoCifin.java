package com.marketplace.validation.domain;

/**
 * Propósito: resumen de línea CIFIN homologada para el solicitante.
 * Patrón: Value Object.
 * Responsabilidad: encapsular calificación y estado legible sin depender del formato de archivo.
 */
public record ResultadoCifin(
        int indicadorRiesgo,
        String estadoLinea,
        boolean informacionEncontrada
) {
}
