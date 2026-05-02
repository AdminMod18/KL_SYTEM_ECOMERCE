package com.marketplace.solicitud.notification;

import com.marketplace.solicitud.model.SolicitudEstado;

import java.time.Instant;

/**
 * Envío de avisos ante resolución de solicitud (mock / adaptador hacia notification-service).
 */
public interface SolicitudEstadoNotificador {

    /**
     * Invocado al cerrar evaluación (APROBADA, RECHAZADA o DEVUELTA). {@code motivoResolucion} alimenta el “por qué” del correo (HU-09).
     */
    void notificarResolucion(
            SolicitudEstado estado,
            Long solicitudId,
            String nombreVendedor,
            String correoElectronico,
            String motivoResolucion);

    /**
     * Invocado por el job de suscripción al pasar a {@link SolicitudEstado#EN_MORA} o {@link SolicitudEstado#CANCELADA} por mora prolongada.
     *
     * @param vencimientoReferencia fecha de vencimiento de la cuota que disparó el evento (la suscripción impaga).
     */
    void notificarSuscripcion(
            SolicitudEstado estadoAlcanzado,
            Long solicitudId,
            String nombreVendedor,
            String correoElectronico,
            Instant vencimientoReferencia);

    /**
     * Invocado al pasar a {@link SolicitudEstado#CANCELADA} por política de reputación (§6).
     *
     * @param detalleMotivo texto breve con métricas (malas / promedio) para correo o log de integración.
     */
    void notificarCancelacionPorReputacion(
            Long solicitudId,
            String nombreVendedor,
            String correoElectronico,
            String detalleMotivo);
}
