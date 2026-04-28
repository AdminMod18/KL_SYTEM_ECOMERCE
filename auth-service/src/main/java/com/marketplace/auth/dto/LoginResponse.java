package com.marketplace.auth.dto;

import java.util.List;

/**
 * Propósito: resultado de login con token y roles efectivos.
 * Patrón: DTO de salida.
 * Responsabilidad: exponer token JWT y metadatos mínimos al cliente.
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        List<String> roles
) {
}
