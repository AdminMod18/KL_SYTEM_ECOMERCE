package com.marketplace.notification.observer;

import com.marketplace.notification.dto.TipoEventoNotificacion;

/**
 * Proposito: evento interno observado por suscriptores de notificacion.
 * Patron: objeto de evento del Observer pattern.
 * Responsabilidad: encapsular tipo de evento y datos de negocio minimos.
 */
public record NotificacionEvent(
        TipoEventoNotificacion tipoEvento,
        String emailDestino,
        String nombreActor,
        String referencia
) {
}
