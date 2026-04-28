package com.marketplace.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Propósito: fallo de autenticación o token inválido mapeable a HTTP 401.
 * Patrón: excepción de dominio de seguridad.
 * Responsabilidad: transportar estado HTTP y mensaje estable para el manejador global.
 */
public class AuthException extends RuntimeException {

    private final HttpStatus status;

    public AuthException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
