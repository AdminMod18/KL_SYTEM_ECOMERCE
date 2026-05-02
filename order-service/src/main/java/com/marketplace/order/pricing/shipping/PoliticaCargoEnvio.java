package com.marketplace.order.pricing.shipping;

import com.marketplace.order.domain.BorradorOrden;

import java.math.BigDecimal;

/**
 * Propósito: calcular el costo de envío según contexto del borrador (Strategy por región).
 * Patrón: Strategy (interfaz intercambiable de política de negocio).
 * Responsabilidad: desacoplar reglas de ciudad/país del decorador de totales.
 */
public interface PoliticaCargoEnvio {

    BigDecimal cargo(BorradorOrden borrador);
}
