package com.marketplace.order.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Propósito: respuesta tras ejecutar el comando de creación de orden.
 * Patrón: DTO de salida.
 * Responsabilidad: exponer identificador persistido, desglose de precios (base, IVA, comisión, envío) y total.
 */
public record OrdenResponse(
        Long ordenId,
        String clienteId,
        String paisEnvio,
        String ciudadEnvio,
        String direccionEnvio,
        BigDecimal subtotalBase,
        BigDecimal montoIva,
        BigDecimal montoComision,
        BigDecimal montoEnvio,
        BigDecimal total,
        String tipoEntrega,
        String estado,
        Instant creadoEn
) {
}
