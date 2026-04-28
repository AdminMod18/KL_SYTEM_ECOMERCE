package com.marketplace.solicitud.state.impl;

import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.state.AbstractEstadoSolicitud;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Propósito: solicitud validada y lista para activación comercial en el marketplace.
 * Patrón: State (estado concreto).
 * Responsabilidad: permitir paso a operación activa o cancelación antes de publicar catálogo.
 */
@Component
public class AprobadaEstado extends AbstractEstadoSolicitud {

    public AprobadaEstado() {
        super(SolicitudEstado.APROBADA,
                Set.of(SolicitudEstado.ACTIVA, SolicitudEstado.CANCELADA));
    }
}
