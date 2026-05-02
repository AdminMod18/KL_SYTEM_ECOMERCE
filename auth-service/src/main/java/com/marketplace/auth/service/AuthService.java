package com.marketplace.auth.service;

import com.marketplace.auth.dto.LoginRequest;
import com.marketplace.auth.dto.LoginResponse;
import com.marketplace.auth.dto.RolesResponse;
import com.marketplace.auth.dto.SincronizarVendedorRequest;

/**
 * Propósito: contrato de autenticación expuesto al controlador (implementado por real y proxy).
 * Patrón: interfaz de servicio; el Proxy implementa el mismo contrato que el sujeto real.
 * Responsabilidad: login y resolución de roles desde cabecera Authorization.
 */
public interface AuthService {

    LoginResponse login(LoginRequest request);

    RolesResponse rolesDesdeAuthorization(String authorizationHeader);

    /** Emite un JWT nuevo con roles actuales en user-service (o demo), sin pedir contraseña. */
    LoginResponse refreshDesdeAuthorization(String authorizationHeader);

    /**
     * Tras ACTIVA: vuelve a aplicar promoción VENDEDOR según documento/correo de la solicitud y emite JWT con roles vigentes.
     */
    LoginResponse sincronizarVendedorDesdeSolicitud(String authorizationHeader, SincronizarVendedorRequest body);
}
