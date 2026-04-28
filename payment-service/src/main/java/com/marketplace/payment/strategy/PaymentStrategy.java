package com.marketplace.payment.strategy;

import com.marketplace.payment.dto.PagoRequest;
import com.marketplace.payment.dto.PagoResponse;
import com.marketplace.payment.model.TipoPago;

/**
 * Propósito: contrato intercambiable para ejecutar la lógica de cobro según el canal.
 * Patrón: Strategy (algoritmo encapsulado y seleccionable en tiempo de ejecución).
 * Responsabilidad: validar datos propios del canal y devolver un {@link PagoResponse} coherente.
 */
public interface PaymentStrategy {

    TipoPago tipo();

    PagoResponse procesar(PagoRequest request);
}
