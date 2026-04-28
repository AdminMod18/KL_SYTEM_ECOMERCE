package com.marketplace.notification.dto;

/**
 * Proposito: eventos de negocio que disparan notificaciones.
 * Patron: enum de dominio para Observer.
 * Responsabilidad: identificar tipo de evento para rutear logica de observadores.
 */
public enum TipoEventoNotificacion {
    COMPRA,
    SOLICITUD_APROBADA
}
