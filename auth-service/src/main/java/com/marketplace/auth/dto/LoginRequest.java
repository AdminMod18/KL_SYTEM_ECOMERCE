package com.marketplace.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Propósito: credenciales para iniciar sesión.
 * Patrón: DTO de entrada.
 * Responsabilidad: transportar usuario y contraseña en texto plano solo sobre HTTPS en despliegue real.
 */
@Getter
@Setter
public class LoginRequest {

    @NotBlank
    @Size(max = 64)
    private String username;

    @NotBlank
    @Size(max = 128)
    private String password;
}
