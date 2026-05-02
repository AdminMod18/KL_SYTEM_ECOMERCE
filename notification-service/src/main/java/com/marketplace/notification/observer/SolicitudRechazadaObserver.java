package com.marketplace.notification.observer;

import com.marketplace.notification.dto.TipoEventoNotificacion;
import org.springframework.stereotype.Component;

@Component
public class SolicitudRechazadaObserver implements NotificacionObserver {

    @Override
    public TipoEventoNotificacion soporta() {
        return TipoEventoNotificacion.SOLICITUD_RECHAZADA;
    }

    @Override
    public String onEvent(NotificacionEvent event) {
        return "Email certificado SOLICITUD_RECHAZADA a "
                + event.emailDestino()
                + " ref="
                + event.referencia()
                + " ("
                + event.nombreActor()
                + ").";
    }
}
