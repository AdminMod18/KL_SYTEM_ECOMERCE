package com.marketplace.user.dto;

import com.marketplace.user.model.TipoPersonaComprador;

import java.time.Instant;
import java.util.List;

public record UsuarioResponse(
        Long id,
        String nombreUsuario,
        String email,
        String nombreCompleto,
        String nombres,
        String apellidos,
        String direccionResidencia,
        String redSocialTwitter,
        String redSocialInstagram,
        String telefono,
        String paisResidencia,
        String ciudadResidencia,
        String documentoIdentidad,
        TipoPersonaComprador tipoPersona,
        List<String> roles,
        Instant creadoEn,
        Instant actualizadoEn) {
}
