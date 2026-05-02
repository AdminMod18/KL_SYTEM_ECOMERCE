package com.marketplace.product.dto;

import com.marketplace.product.model.ProductoInteraccionTipo;

import java.time.Instant;

public record ProductoInteraccionResponse(
        Long id,
        Long productoId,
        ProductoInteraccionTipo tipo,
        String contenido,
        String autorNombre,
        String respuesta,
        Instant creadoEn) {}
