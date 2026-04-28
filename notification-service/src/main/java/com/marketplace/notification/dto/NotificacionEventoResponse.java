package com.marketplace.notification.dto;

import java.util.List;

/**
 * Proposito: resultado de procesamiento del evento por el subject observer.
 * Patron: DTO de salida.
 * Responsabilidad: informar observadores ejecutados y mensajes generados.
 */
public record NotificacionEventoResponse(
        TipoEventoNotificacion tipoEvento,
        String referencia,
        int observadoresEjecutados,
        List<String> mensajes
) {
}
