package com.marketplace.solicitud.exception;

/**
 * Propósito: fallo de reglas de negocio en solicitudes (validación encadenada o transiciones).
 * Patrón: Excepción de dominio / aplicación.
 * Responsabilidad: transportar código estable para el manejador global HTTP.
 */
public class SolicitudBusinessException extends RuntimeException {

    private final String codigo;

    public SolicitudBusinessException(String codigo, String message) {
        super(message);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
