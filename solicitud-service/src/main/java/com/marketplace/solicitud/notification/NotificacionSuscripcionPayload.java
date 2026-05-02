package com.marketplace.solicitud.notification;

/**
 * Cuerpo JSON alineado con {@code POST /notificaciones/eventos} del notification-service.
 */
public record NotificacionSuscripcionPayload(
        String tipoEvento, String emailDestino, String nombreActor, String referencia) {}
