package com.marketplace.order.dto;

import java.math.BigDecimal;

/** Línea de detalle en listado de pedidos (HU-20). */
public record OrdenLineaListItemResponse(
        String sku, int cantidad, BigDecimal precioUnitario, BigDecimal subtotalLinea) {}
