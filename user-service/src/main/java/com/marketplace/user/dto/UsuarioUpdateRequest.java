package com.marketplace.user.dto;

import com.marketplace.user.model.TipoPersonaComprador;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @Size(max = 120)
    private String nombres;

    @Size(max = 120)
    private String apellidos;

    @Size(max = 300)
    private String direccionResidencia;

    @Size(max = 80)
    private String redSocialTwitter;

    @Size(max = 80)
    private String redSocialInstagram;

    @Size(max = 40)
    @Pattern(regexp = "^$|^[0-9+()\\-\\s]{7,40}$", message = "Teléfono: entre 7 y 40 caracteres numéricos y símbolos +()- ")
    private String telefono;

    @Size(max = 120)
    private String paisResidencia;

    @Size(max = 120)
    private String ciudadResidencia;

    @Size(max = 32)
    private String documentoIdentidad;

    private TipoPersonaComprador tipoPersona;
}
