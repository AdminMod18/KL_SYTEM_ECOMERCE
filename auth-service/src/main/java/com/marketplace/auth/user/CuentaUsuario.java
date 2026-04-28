package com.marketplace.auth.user;

import java.util.List;

/**
 * Propósito: representar credenciales almacenadas y roles asociados a un usuario demo.
 * Patrón: Value Object / registro inmutable.
 * Responsabilidad: conservar hash BCrypt y roles autorizados.
 */
public record CuentaUsuario(String passwordHash, List<String> roles) {
}
