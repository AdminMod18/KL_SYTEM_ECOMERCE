package com.marketplace.solicitud.state.impl;

import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.state.AbstractEstadoSolicitud;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Propósito: solicitud denegada en evaluación (documentación o riesgo).
 * Patrón: State (estado concreto / terminal parcial).
 * Responsabilidad: no reabrir a PENDIENTE ni APROBAR sin nuevo proceso; solo CANCELADA administrativa.
 */
@Component
public class RechazadaEstado extends AbstractEstadoSolicitud {

    public RechazadaEstado() {
        super(SolicitudEstado.RECHAZADA,
                Set.of(SolicitudEstado.CANCELADA));
    }

    @Override
    public String descripcionFlujo() {
        return "Rechazada: no admite validación automática ni retorno a PENDIENTE; solo CANCELADA.";
    }
}
