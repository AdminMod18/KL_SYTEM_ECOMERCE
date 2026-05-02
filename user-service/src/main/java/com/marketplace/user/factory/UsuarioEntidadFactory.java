package com.marketplace.user.factory;

import com.marketplace.user.dto.UsuarioCreateRequest;
import com.marketplace.user.entity.Usuario;

import java.util.ArrayList;
import java.util.List;

public final class UsuarioEntidadFactory {

    private UsuarioEntidadFactory() {}

    public static Usuario desdeAlta(UsuarioCreateRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(request.getNombreUsuario());
        usuario.setEmail(request.getEmail());
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setNombres(vacioANulo(request.getNombres()));
        usuario.setApellidos(vacioANulo(request.getApellidos()));
        usuario.setDireccionResidencia(vacioANulo(request.getDireccionResidencia()));
        usuario.setRedSocialTwitter(vacioANulo(request.getRedSocialTwitter()));
        usuario.setRedSocialInstagram(vacioANulo(request.getRedSocialInstagram()));
        usuario.setTelefono(vacioANulo(request.getTelefono()));
        usuario.setPaisResidencia(vacioANulo(request.getPaisResidencia()));
        usuario.setCiudadResidencia(vacioANulo(request.getCiudadResidencia()));
        usuario.setDocumentoIdentidad(vacioANulo(request.getDocumentoIdentidad()));
        usuario.setTipoPersona(request.getTipoPersona());
        usuario.setRoles(new ArrayList<>(List.of("COMPRADOR", "USER")));
        return usuario;
    }

    private static String vacioANulo(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
