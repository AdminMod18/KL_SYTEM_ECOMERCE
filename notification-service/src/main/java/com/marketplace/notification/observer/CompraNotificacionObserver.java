package com.marketplace.notification.observer;

import com.marketplace.notification.dto.TipoEventoNotificacion;
import org.springframework.stereotype.Component;

/**
 * Proposito: reaccionar a eventos de compra para enviar confirmacion al comprador.
 * Patron: Observer concreto para el tipo COMPRA.
 * Responsabilidad: generar mensaje de notificacion de compra procesada.
 */
@Component
public class CompraNotificacionObserver implements NotificacionObserver {

    @Override
    public TipoEventoNotificacion soporta() {
        return TipoEventoNotificacion.COMPRA;
    }

    @Override
    public String onEvent(NotificacionEvent event) {
        return "Email COMPRA enviado a " + event.emailDestino()
                + " por referencia " + event.referencia()
                + " (cliente: " + event.nombreActor() + ").";
    }
}
