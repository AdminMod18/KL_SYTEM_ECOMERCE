package com.marketplace.auth.service;

import com.marketplace.auth.dto.LoginRequest;
import com.marketplace.auth.dto.LoginResponse;
import com.marketplace.auth.dto.RolesResponse;

/**
 * Propósito: contrato de autenticación expuesto al controlador (implementado por real y proxy).
 * Patrón: interfaz de servicio; el Proxy implementa el mismo contrato que el sujeto real.
 * Responsabilidad: login y resolución de roles desde cabecera Authorization.
 */
public interface AuthService {

    LoginResponse login(LoginRequest request);

    RolesResponse rolesDesdeAuthorization(String authorizationHeader);
}
