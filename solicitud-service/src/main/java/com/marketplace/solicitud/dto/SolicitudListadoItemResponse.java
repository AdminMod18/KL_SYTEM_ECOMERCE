package com.marketplace.solicitud.dto;

import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.model.TipoPersonaSolicitante;

import java.time.Instant;

/**
 * Propósito: fila de tabla para consulta operativa (director / backoffice) sin adjuntos.
 * Patrón: DTO (record inmutable).
 * Responsabilidad: respuesta liviana en {@code GET /solicitudes}; el detalle completo sigue en {@link SolicitudResponse}.
 */
public record SolicitudListadoItemResponse(
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
        SolicitudEstado estado,
        Instant creadoEn,
        Instant proximoVencimientoSuscripcion) {
}
