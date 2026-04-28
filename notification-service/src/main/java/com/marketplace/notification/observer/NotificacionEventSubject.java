package com.marketplace.notification.observer;

import com.marketplace.notification.dto.TipoEventoNotificacion;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Proposito: subject central que notifica a observers suscritos por tipo de evento.
 * Patron: Observer (Subject).
 * Responsabilidad: enrutar eventos a observadores compatibles y devolver mensajes resultantes.
 */
@Component
public class NotificacionEventSubject {

    private final List<NotificacionObserver> observers;

    public NotificacionEventSubject(List<NotificacionObserver> observers) {
        this.observers = observers;
    }

    public List<String> publicar(NotificacionEvent event) {
        TipoEventoNotificacion tipo = event.tipoEvento();
        return observers.stream()
                .filter(o -> o.soporta() == tipo)
                .map(o -> o.onEvent(event))
                .toList();
    }
}
