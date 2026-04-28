package com.marketplace.solicitud.state;

import com.marketplace.solicitud.model.SolicitudEstado;

/**
 * Propósito: contrato polimórfico asociado a cada valor de {@link SolicitudEstado}.
 * Patrón: State (rol <em>State</em> del catálogo GoF).
 * Responsabilidad: encapsular las transiciones válidas desde un estado concreto sin condicionales dispersos en servicios.
 */
public interface SolicitudEstadoBehavior {

    SolicitudEstado estado();

    void assertTransicionPermitida(SolicitudEstado destino);
}
