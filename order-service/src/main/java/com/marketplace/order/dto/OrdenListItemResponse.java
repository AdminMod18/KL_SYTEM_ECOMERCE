package com.marketplace.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Listado enriquecido para «Mis pedidos» (HU-20). */
public record OrdenListItemResponse(
        Long ordenId,
        String clienteId,
        BigDecimal total,
        BigDecimal subtotalBase,
        BigDecimal montoIva,
        BigDecimal montoComision,
        BigDecimal montoEnvio,
        String tipoEntrega,
        String estado,
        Instant creadoEn,
        int numeroLineas,
        List<OrdenLineaListItemResponse> lineas) {}
