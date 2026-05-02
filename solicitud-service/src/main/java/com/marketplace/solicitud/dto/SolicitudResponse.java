package com.marketplace.solicitud.dto;

import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.model.TipoPersonaSolicitante;

import java.time.Instant;
import java.util.List;

/**
 * Propósito: salida REST con vista estable de una solicitud y sus adjuntos.
 * Patrón: DTO (record inmutable).
 * Responsabilidad: exponer solo campos necesarios al consumidor y tipos serializables.
 */
public record SolicitudResponse(
        Long id,
        String numeroRadicado,
        String nombreVendedor,
        String nombres,
        String apellidos,
        String documentoIdentidad,
        String correoElectronico,
        String paisResidencia,
        String ciudadResidencia,
        String telefono,
        TipoPersonaSolicitante tipoPersona,
        List<AdjuntoResponse> adjuntos,
        SolicitudEstado estado,
        Instant creadoEn,
        Instant proximoVencimientoSuscripcion) {
}
