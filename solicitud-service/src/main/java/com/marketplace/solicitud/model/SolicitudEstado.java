package com.marketplace.solicitud.model;

/**
 * Propósito: catálogo persistente de estados posibles de una solicitud de vendedor.
 * Patrón: Type Safe Enum (complemento al State pattern orientado a objetos).
 * Responsabilidad: representar valores almacenados en base de datos y API REST.
 */
public enum SolicitudEstado {
    PENDIENTE,
    APROBADA,
    RECHAZADA,
    DEVUELTA,
    ACTIVA,
    EN_MORA,
    CANCELADA
}
