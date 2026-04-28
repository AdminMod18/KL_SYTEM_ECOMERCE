package com.marketplace.payment.dto;

import com.marketplace.payment.model.TipoPago;

import java.util.UUID;

/**
 * Propósito: resultado homogéneo del procesamiento de un pago simulado.
 * Patrón: DTO de salida.
 * Responsabilidad: exponer identificador de transacción y estado legible para clientes y auditoría.
 */
public record PagoResponse(
        UUID idTransaccion,
        String estado,
        String mensaje,
        TipoPago tipo
) {
}
