package com.marketplace.user.dto;

import java.time.Instant;

/**
 * Propósito: vista REST de un usuario persistido.
 * Patrón: DTO de salida.
 * Responsabilidad: exponer identificador, datos públicos y auditoría mínima.
 */
public record UsuarioResponse(
        Long id,
        String nombreUsuario,
        String email,
        String nombreCompleto,
        Instant creadoEn,
        Instant actualizadoEn
) {
}
