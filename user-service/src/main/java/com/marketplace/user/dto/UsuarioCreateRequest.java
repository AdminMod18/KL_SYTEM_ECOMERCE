package com.marketplace.user.dto;

import com.marketplace.user.model.TipoPersonaComprador;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Alta de comprador. Caso estudio §7: nombres y apellidos separados o {@code nombreCompleto} (compatibilidad API).
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

    /**
     * Contraseña de registro (persistida como BCrypt). Opcional para compatibilidad con clientes que solo crean perfil.
     */
    @Size(min = 8, max = 128)
    private String password;

    /**
     * Opcional en JSON si vienen {@code nombres} y {@code apellidos} no vacíos; el servicio también puede derivarlo.
     * No usar {@code @NotBlank} aquí: la regla {@link #isNombreParaAltaValido()} acepta una u otra forma (caso estudio §7).
     */
    @Size(max = 120)
    private String nombreCompleto;

    @Size(max = 120)
    private String nombres;

    @Size(max = 120)
    private String apellidos;

    @AssertTrue(message = "Indique nombreCompleto o bien nombres y apellidos (comprador, caso estudio).")
    public boolean isNombreParaAltaValido() {
        boolean completo = nombreCompleto != null && !nombreCompleto.isBlank();
        boolean partes =
                nombres != null
                        && !nombres.isBlank()
                        && apellidos != null
                        && !apellidos.isBlank();
        return completo || partes;
    }

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
