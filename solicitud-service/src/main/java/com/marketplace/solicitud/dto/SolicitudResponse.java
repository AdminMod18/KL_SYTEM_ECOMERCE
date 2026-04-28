package com.marketplace.solicitud.dto;

import com.marketplace.solicitud.model.SolicitudEstado;

import java.time.Instant;

/**
 * Propósito: salida REST con vista estable de una solicitud.
 * Patrón: DTO (Data Transfer Object).
 * Responsabilidad: exponer solo campos necesarios al consumidor y tipos serializables.
 */
public record SolicitudResponse(
        Long id,
        String nombreVendedor,
        String documentoIdentidad,
        SolicitudEstado estado,
        Instant creadoEn
) {
}
