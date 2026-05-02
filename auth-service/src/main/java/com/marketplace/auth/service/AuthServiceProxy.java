package com.marketplace.auth.service;

import com.marketplace.auth.dto.LoginRequest;
import com.marketplace.auth.dto.LoginResponse;
import com.marketplace.auth.dto.RolesResponse;
import com.marketplace.auth.dto.SincronizarVendedorRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Propósito: envolver el servicio real de autenticación para añadir auditoría/logging sin duplicar reglas.
 * Patrón: Proxy (control de acceso transversal + delegación al objeto real).
 * Responsabilidad: interceptar llamadas, registrar trazas y delegar en {@link AuthServiceImpl}.
 */
@Service
@Primary
public class AuthServiceProxy implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceProxy.class);

    private final AuthService delegado;

    public AuthServiceProxy(@Qualifier("authServiceReal") AuthService delegado) {
        this.delegado = delegado;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("[Proxy] Intento de login usuario={}", request.getUsername());
        try {
            LoginResponse respuesta = delegado.login(request);
            log.info("[Proxy] Login exitoso usuario={}", request.getUsername());
            return respuesta;
        } catch (RuntimeException ex) {
            log.warn("[Proxy] Login fallido usuario={} causa={}", request.getUsername(), ex.getMessage());
            throw ex;
        }
    }

    @Override
    public RolesResponse rolesDesdeAuthorization(String authorizationHeader) {
        log.info("[Proxy] Consulta de roles desde Authorization");
        return delegado.rolesDesdeAuthorization(authorizationHeader);
    }

    @Override
    public LoginResponse refreshDesdeAuthorization(String authorizationHeader) {
        log.info("[Proxy] Refresh de JWT desde Authorization");
        return delegado.refreshDesdeAuthorization(authorizationHeader);
    }

    @Override
    public LoginResponse sincronizarVendedorDesdeSolicitud(
            String authorizationHeader, SincronizarVendedorRequest body) {
        log.info("[Proxy] Sincronizar VENDEDOR desde solicitud id={}", body.getSolicitudId());
        return delegado.sincronizarVendedorDesdeSolicitud(authorizationHeader, body);
    }
}
