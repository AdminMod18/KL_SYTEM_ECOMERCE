package com.marketplace.notification.observer;

import com.marketplace.notification.dto.TipoEventoNotificacion;
import org.springframework.stereotype.Component;

@Component
public class SolicitudDevueltaObserver implements NotificacionObserver {

    @Override
    public TipoEventoNotificacion soporta() {
        return TipoEventoNotificacion.SOLICITUD_DEVUELTA;
    }

    @Override
    public String onEvent(NotificacionEvent event) {
        return "Email certificado SOLICITUD_DEVUELTA a "
                + event.emailDestino()
                + " ref="
                + event.referencia()
                + " ("
                + event.nombreActor()
                + ").";
    }
}
