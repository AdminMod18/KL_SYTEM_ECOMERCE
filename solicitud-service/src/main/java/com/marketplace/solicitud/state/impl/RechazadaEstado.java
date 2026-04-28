package com.marketplace.solicitud.state.impl;

import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.state.AbstractEstadoSolicitud;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Propósito: solicitud denegada en evaluación (documentación o riesgo).
 * Patrón: State (estado concreto / terminal parcial).
 * Responsabilidad: solo permitir cierre administrativo explícito (CANCELADA).
 */
@Component
public class RechazadaEstado extends AbstractEstadoSolicitud {

    public RechazadaEstado() {
        super(SolicitudEstado.RECHAZADA,
                Set.of(SolicitudEstado.CANCELADA));
    }
}
