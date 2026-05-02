package com.marketplace.notification.observer;

import com.marketplace.notification.dto.TipoEventoNotificacion;
import org.springframework.stereotype.Component;

/**
 * Observer para cancelacion de solicitud por politica de reputacion.
 */
@Component
public class SolicitudCancelacionReputacionObserver implements NotificacionObserver {

    @Override
    public TipoEventoNotificacion soporta() {
        return TipoEventoNotificacion.SOLICITUD_CANCELACION_REPUTACION;
    }

    @Override
    public String onEvent(NotificacionEvent event) {
        return "Email SOLICITUD_CANCELACION_REPUTACION a "
                + event.emailDestino()
                + " (vendedor "
                + event.nombreActor()
                + ", ref "
                + event.referencia()
                + ").";
    }
}
