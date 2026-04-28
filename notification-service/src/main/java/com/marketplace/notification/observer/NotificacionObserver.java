package com.marketplace.notification.observer;

import com.marketplace.notification.dto.TipoEventoNotificacion;

/**
 * Proposito: contrato de observadores que reaccionan a eventos del subject.
 * Patron: Observer (Observer interface).
 * Responsabilidad: declarar interes por tipo y procesar el evento.
 */
public interface NotificacionObserver {

    TipoEventoNotificacion soporta();

    String onEvent(NotificacionEvent event);
}
