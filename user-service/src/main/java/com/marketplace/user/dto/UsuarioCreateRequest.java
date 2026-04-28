package com.marketplace.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Propósito: datos para crear un usuario vía API.
 * Patrón: DTO de entrada.
 * Responsabilidad: validar campos obligatorios y límites de tamaño.
 */
@Getter
@Setter
public class UsuarioCreateRequest {

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
