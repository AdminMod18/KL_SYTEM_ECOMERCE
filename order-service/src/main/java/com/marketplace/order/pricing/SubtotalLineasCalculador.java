package com.marketplace.order.pricing;

import com.marketplace.order.domain.BorradorOrden;

import java.math.BigDecimal;

/**
 * Propósito: calcular el subtotal bruto sumando cantidad × precio unitario por línea.
 * Patrón: ConcreteComponent del Decorator (núcleo sin impuestos ni cargos adicionales).
 * Responsabilidad: única fuente del subtotal antes de capas decoradoras.
 */
public class SubtotalLineasCalculador implements CalculadorPrecioOrden {

    @Override
    public BigDecimal calcular(BorradorOrden borrador) {
        return borrador.subtotalLineas();
    }
}
