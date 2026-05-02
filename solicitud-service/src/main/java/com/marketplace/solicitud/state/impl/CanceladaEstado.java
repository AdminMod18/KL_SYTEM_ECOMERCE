package com.marketplace.solicitud.state.impl;

import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.state.AbstractEstadoSolicitud;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * Propósito: estado terminal sin más transiciones de negocio.
 * Patrón: State (estado concreto terminal).
 * Responsabilidad: bloquear cualquier cambio posterior salvo futuras extensiones explícitas.
 */
@Component
public class CanceladaEstado extends AbstractEstadoSolicitud {

    public CanceladaEstado() {
        super(SolicitudEstado.CANCELADA,
                Collections.<SolicitudEstado>emptySet());
    }

    @Override
    public String descripcionFlujo() {
        return "Cancelada: estado terminal; no admite nuevas transiciones de negocio.";
    }
}
