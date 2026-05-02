package com.marketplace.solicitud.state.impl;

import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.state.AbstractEstadoSolicitud;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Propósito: incumplimiento de obligaciones (p. ej. pagos de comisión) sin cierre de cuenta.
 * Patrón: State (estado concreto).
 * Responsabilidad: permitir regularización volviendo a ACTIVA o cerrar con CANCELADA.
 */
@Component
public class EnMoraEstado extends AbstractEstadoSolicitud {

    public EnMoraEstado() {
        super(SolicitudEstado.EN_MORA,
                Set.of(SolicitudEstado.ACTIVA, SolicitudEstado.CANCELADA));
    }

    @Override
    public String descripcionFlujo() {
        return "En mora: regularización a ACTIVA o cierre CANCELADA.";
    }
}
