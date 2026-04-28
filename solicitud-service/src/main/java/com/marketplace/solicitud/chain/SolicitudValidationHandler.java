package com.marketplace.solicitud.chain;

import com.marketplace.solicitud.dto.SolicitudCreateRequest;

/**
 * Propósito: eslabón base para validar solicitudes nuevas antes de persistir.
 * Patrón: Chain of Responsibility (manejador abstracto con sucesor opcional).
 * Responsabilidad: definir el contrato `procesar` y el encadenamiento secuencial sin que el cliente conozca todos los pasos.
 */
public abstract class SolicitudValidationHandler {

    private SolicitudValidationHandler siguiente;

    /**
     * Encadena el siguiente manejador y devuelve el siguiente para fluencia tipo builder.
     */
    public SolicitudValidationHandler enlazar(SolicitudValidationHandler siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    /**
     * Recorre la cadena invocando cada eslabón en orden.
     */
    public final void validar(SolicitudCreateRequest solicitud) {
        procesar(solicitud);
        if (siguiente != null) {
            siguiente.validar(solicitud);
        }
    }

    /**
     * Validación concreta del eslabón; debe lanzar {@link com.marketplace.solicitud.exception.SolicitudBusinessException} si falla.
     */
    protected abstract void procesar(SolicitudCreateRequest solicitud);
}
