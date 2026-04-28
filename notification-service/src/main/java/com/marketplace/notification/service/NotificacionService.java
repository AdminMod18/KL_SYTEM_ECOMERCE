package com.marketplace.notification.service;

import com.marketplace.notification.dto.NotificacionEventoRequest;
import com.marketplace.notification.dto.NotificacionEventoResponse;
import com.marketplace.notification.observer.NotificacionEvent;
import com.marketplace.notification.observer.NotificacionEventSubject;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Proposito: caso de uso para procesar eventos y disparar observers de notificacion.
 * Patron: Application Service sobre Subject Observer.
 * Responsabilidad: mapear request a evento interno y construir respuesta de salida.
 */
@Service
public class NotificacionService {

    private final NotificacionEventSubject subject;

    public NotificacionService(NotificacionEventSubject subject) {
        this.subject = subject;
    }

    public NotificacionEventoResponse procesar(NotificacionEventoRequest request) {
        NotificacionEvent event = new NotificacionEvent(
                request.getTipoEvento(),
                request.getEmailDestino().trim(),
                request.getNombreActor().trim(),
                request.getReferencia().trim());
        List<String> mensajes = subject.publicar(event);
        return new NotificacionEventoResponse(
                event.tipoEvento(),
                event.referencia(),
                mensajes.size(),
                mensajes);
    }
}
