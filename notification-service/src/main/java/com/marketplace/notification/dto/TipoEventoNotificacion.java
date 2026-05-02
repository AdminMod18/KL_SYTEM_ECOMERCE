package com.marketplace.notification.dto;

/**
 * Proposito: eventos de negocio que disparan notificaciones.
 * Patron: enum de dominio para Observer.
 * Responsabilidad: identificar tipo de evento para rutear logica de observadores.
 */
public enum TipoEventoNotificacion {
    COMPRA,
    SOLICITUD_APROBADA,
    /** Resultado negativo de evaluación con motivo en referencia (HU-09). */
    SOLICITUD_RECHAZADA,
    /** Solicitud devuelta para corrección (HU-09). */
    SOLICITUD_DEVUELTA,
    SOLICITUD_MORA,
    SOLICITUD_CANCELACION_SUSCRIPCION,
    /** Baja automatica por reputacion del vendedor (caso estudio). */
    SOLICITUD_CANCELACION_REPUTACION
}
