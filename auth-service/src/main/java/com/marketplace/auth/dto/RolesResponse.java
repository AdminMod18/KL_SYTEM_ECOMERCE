package com.marketplace.auth.dto;

import java.util.List;

/**
 * Propósito: roles resueltos a partir de un JWT válido.
 * Patrón: DTO de salida.
 * Responsabilidad: devolver subject y lista de roles sin exponer el secreto.
 */
public record RolesResponse(
        String username,
        List<String> roles
) {
}
