package com.marketplace.user.exception;

/**
 * Propósito: conflicto de negocio (duplicados de usuario o email).
 * Patrón: excepción de dominio.
 * Responsabilidad: transportar código estable para el manejador global HTTP.
 */
public class UsuarioBusinessException extends RuntimeException {

    private final String codigo;

    public UsuarioBusinessException(String codigo, String message) {
        super(message);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
