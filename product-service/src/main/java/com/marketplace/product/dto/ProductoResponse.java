package com.marketplace.product.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Propósito: vista REST estable de un producto publicado.
 * Patrón: DTO de salida.
 * Responsabilidad: exponer datos del catálogo sin acoplar la entidad.
 */
public record ProductoResponse(
        Long id,
        String nombre,
        BigDecimal precio,
        String descripcion,
        String rutaCategoria,
        Instant creadoEn
) {
}
