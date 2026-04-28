package com.marketplace.solicitud.state.impl;

import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.state.AbstractEstadoSolicitud;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Propósito: vendedor habilitado para vender bajo condiciones contractuales vigentes.
 * Patrón: State (estado concreto).
 * Responsabilidad: modelar mora financiera o baja voluntaria/forzosa.
 */
@Component
public class ActivaEstado extends AbstractEstadoSolicitud {

    public ActivaEstado() {
        super(SolicitudEstado.ACTIVA,
                Set.of(SolicitudEstado.EN_MORA, SolicitudEstado.CANCELADA));
    }
}
