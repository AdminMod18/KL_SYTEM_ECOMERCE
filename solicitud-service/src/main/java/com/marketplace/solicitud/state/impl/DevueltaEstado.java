package com.marketplace.solicitud.state.impl;

import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.state.AbstractEstadoSolicitud;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Propósito: solicitud devuelta al vendedor para subsanar información.
 * Patrón: State (estado concreto).
 * Responsabilidad: permite {@code POST /validacion-automatica} de nuevo (mismos destinos que desde PENDIENTE),
 * además de pasar manualmente a PENDIENTE o CANCELADA.
 */
@Component
public class DevueltaEstado extends AbstractEstadoSolicitud {

    public DevueltaEstado() {
        super(
                SolicitudEstado.DEVUELTA,
                Set.of(
                        SolicitudEstado.PENDIENTE,
                        SolicitudEstado.CANCELADA,
                        SolicitudEstado.APROBADA,
                        SolicitudEstado.RECHAZADA,
                        SolicitudEstado.DEVUELTA));
    }

    @Override
    public boolean permiteValidacionAutomatica() {
        return true;
    }

    @Override
    public String descripcionFlujo() {
        return "Devuelta: reintentar validación automática (APROBADA/RECHAZADA/DEVUELTA), volver a PENDIENTE o cancelar.";
    }
}
