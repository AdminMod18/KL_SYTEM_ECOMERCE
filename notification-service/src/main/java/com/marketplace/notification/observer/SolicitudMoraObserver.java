package com.marketplace.notification.observer;

import com.marketplace.notification.dto.TipoEventoNotificacion;
import org.springframework.stereotype.Component;

/**
 * Observer para avisar mora de suscripción del vendedor.
 */
@Component
public class SolicitudMoraObserver implements NotificacionObserver {

    @Override
    public TipoEventoNotificacion soporta() {
        return TipoEventoNotificacion.SOLICITUD_MORA;
    }

    @Override
    public String onEvent(NotificacionEvent event) {
        return "Email SOLICITUD_MORA a "
                + event.emailDestino()
                + " (vendedor "
                + event.nombreActor()
                + ", ref "
                + event.referencia()
                + ").";
    }
}
