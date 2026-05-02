package com.marketplace.order.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** Listado liviano para “Mis pedidos” (HU-20). */
public record OrdenListItemResponse(
        Long ordenId,
        String clienteId,
        BigDecimal total,
        String tipoEntrega,
        Instant creadoEn,
        int numeroLineas) {}
