package com.marketplace.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Propósito: datos para actualizar un usuario existente.
 * Patrón: DTO de entrada.
 * Responsabilidad: permitir modificar email y nombre completo manteniendo nombreUsuario estable o actualizable según reglas del servicio.
 */
@Getter
@Setter
public class UsuarioUpdateRequest {

    @NotBlank
    @Size(min = 3, max = 64)
    private String nombreUsuario;

    @NotBlank
    @Email
    @Size(max = 120)
    private String email;

    @NotBlank
    @Size(max = 120)
    private String nombreCompleto;
}
