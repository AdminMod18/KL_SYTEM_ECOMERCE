package com.marketplace.notification.observer;

import com.marketplace.notification.dto.TipoEventoNotificacion;
import org.springframework.stereotype.Component;

/**
 * Proposito: reaccionar cuando una solicitud de vendedor fue aprobada.
 * Patron: Observer concreto para SOLICITUD_APROBADA.
 * Responsabilidad: generar mensaje de alta comercial al solicitante.
 */
@Component
public class SolicitudAprobadaObserver implements NotificacionObserver {

    @Override
    public TipoEventoNotificacion soporta() {
        return TipoEventoNotificacion.SOLICITUD_APROBADA;
    }

    @Override
    public String onEvent(NotificacionEvent event) {
        return "Email SOLICITUD_APROBADA enviado a " + event.emailDestino()
                + " para solicitud " + event.referencia()
                + " (vendedor: " + event.nombreActor() + ").";
    }
}
