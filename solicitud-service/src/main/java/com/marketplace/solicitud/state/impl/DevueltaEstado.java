package com.marketplace.solicitud.state.impl;

import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.state.AbstractEstadoSolicitud;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Propósito: solicitud devuelta al vendedor para subsanar información.
 * Patrón: State (estado concreto).
 * Responsabilidad: permitir reingreso al flujo o cancelación definitiva.
 */
@Component
public class DevueltaEstado extends AbstractEstadoSolicitud {

    public DevueltaEstado() {
        super(SolicitudEstado.DEVUELTA,
                Set.of(SolicitudEstado.PENDIENTE, SolicitudEstado.CANCELADA));
    }
}
