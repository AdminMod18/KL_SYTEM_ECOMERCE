package com.marketplace.solicitud.state.impl;

import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.state.AbstractEstadoSolicitud;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Propósito: estado inicial de revisión documental / compliance.
 * Patrón: State (estado concreto).
 * Responsabilidad: permitir cierre positivo, negativo o devolución para corrección.
 */
@Component
public class PendienteEstado extends AbstractEstadoSolicitud {

    public PendienteEstado() {
        super(SolicitudEstado.PENDIENTE,
                Set.of(SolicitudEstado.APROBADA, SolicitudEstado.RECHAZADA, SolicitudEstado.DEVUELTA));
    }
}
