package com.marketplace.notification.observer;

import com.marketplace.notification.dto.TipoEventoNotificacion;
import org.springframework.stereotype.Component;

/**
 * Observer para baja por mora prolongada de la suscripción.
 */
@Component
public class SolicitudCancelacionSuscripcionObserver implements NotificacionObserver {

    @Override
    public TipoEventoNotificacion soporta() {
        return TipoEventoNotificacion.SOLICITUD_CANCELACION_SUSCRIPCION;
    }

    @Override
    public String onEvent(NotificacionEvent event) {
        return "Email SOLICITUD_CANCELACION_SUSCRIPCION a "
                + event.emailDestino()
                + " (vendedor "
                + event.nombreActor()
                + ", ref "
                + event.referencia()
                + ").";
    }
}
