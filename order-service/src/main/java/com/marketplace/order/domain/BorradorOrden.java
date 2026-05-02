package com.marketplace.order.domain;

import com.marketplace.order.dto.LineaOrdenRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * Propósito: vista de solo lectura del pedido usada por la cadena de cálculo de precios.
 * Patrón: objeto de dominio simple (no JPA).
 * Responsabilidad: exponer líneas y datos de envío para decoradores y políticas sin acoplar a JPA.
 */
public record BorradorOrden(
        List<LineaOrdenRequest> lineas,
        String paisEnvio,
        String ciudadEnvio,
        String direccionEnvio) {

    public BorradorOrden(List<LineaOrdenRequest> lineas) {
        this(lineas, null, null, null);
    }

    public BigDecimal subtotalLineas() {
        return lineas.stream()
                .map(l -> l.getPrecioUnitario().multiply(BigDecimal.valueOf(l.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
