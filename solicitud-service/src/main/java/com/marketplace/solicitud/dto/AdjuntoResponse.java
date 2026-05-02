package com.marketplace.solicitud.dto;

import com.marketplace.solicitud.model.TipoDocumentoAdjunto;

/**
 * Propósito: vista de un adjunto persistido (sin exponer bytes).
 * Patrón: DTO inmutable (record).
 * Responsabilidad: serialización estable hacia clientes REST.
 */
public record AdjuntoResponse(
        TipoDocumentoAdjunto tipo, String nombreArchivo, String uriArchivo) {
}
