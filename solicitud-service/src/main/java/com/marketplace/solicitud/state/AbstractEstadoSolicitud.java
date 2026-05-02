package com.marketplace.solicitud.state;

import com.marketplace.solicitud.exception.SolicitudBusinessException;
import com.marketplace.solicitud.model.SolicitudEstado;

import java.util.Set;

/**
 * Propósito: factor común para estados con lista cerrada de destinos permitidos.
 * Patrón: State (clase base de estados concretos) + Template para validación uniforme.
 * Responsabilidad: centralizar la verificación de pertenencia del destino al conjunto permitido.
 */
public abstract class AbstractEstadoSolicitud implements SolicitudEstadoBehavior {

    private final SolicitudEstado estado;
    private final Set<SolicitudEstado> destinosPermitidos;

    protected AbstractEstadoSolicitud(SolicitudEstado estado, Set<SolicitudEstado> destinosPermitidos) {
        this.estado = estado;
        this.destinosPermitidos = destinosPermitidos;
    }

    @Override
    public SolicitudEstado estado() {
        return estado;
    }

    @Override
    public void assertTransicionPermitida(SolicitudEstado destino) {
        if (destino == estado) {
            return;
        }
        if (!destinosPermitidos.contains(destino)) {
            throw new SolicitudBusinessException(
                    "TRANSICION_INVALIDA",
                    "No está permitido pasar de " + estado + " a " + destino
                            + ". Destinos permitidos desde " + estado + ": " + destinosPermitidos + ".");
        }
    }

    @Override
    public boolean permiteValidacionAutomatica() {
        return false;
    }
}
