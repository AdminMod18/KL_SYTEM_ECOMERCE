package com.marketplace.order.pricing;

import com.marketplace.order.domain.BorradorOrden;

import java.math.BigDecimal;

/**
 * Propósito: contrato intercambiable para obtener el monto total de un borrador de orden.
 * Patrón: Componente del Decorator (Component en GoF) + rol en Strategy-like pipeline.
 * Responsabilidad: definir la operación común que el decorador envuelve y el componente concreto implementa.
 */
public interface CalculadorPrecioOrden {

    BigDecimal calcular(BorradorOrden borrador);
}
