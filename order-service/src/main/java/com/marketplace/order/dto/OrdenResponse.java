package com.marketplace.order.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Propósito: respuesta tras ejecutar el comando de creación de orden.
 * Patrón: DTO de salida.
 * Responsabilidad: exponer identificador persistido, total decorado y marca temporal.
 */
public record OrdenResponse(
        Long ordenId,
        String clienteId,
        BigDecimal total,
        String estado,
        Instant creadoEn
) {
}
