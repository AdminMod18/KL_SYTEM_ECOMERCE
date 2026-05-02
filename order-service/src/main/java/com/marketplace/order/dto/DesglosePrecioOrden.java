package com.marketplace.order.dto;

import java.math.BigDecimal;

/**
 * Desglose de totales para auditoría y validación QA (subtotal base, IVA, comisión, envío, total).
 */
public record DesglosePrecioOrden(
        BigDecimal subtotalBase,
        BigDecimal montoIva,
        BigDecimal montoComision,
        BigDecimal montoEnvio,
        BigDecimal total) {
}
