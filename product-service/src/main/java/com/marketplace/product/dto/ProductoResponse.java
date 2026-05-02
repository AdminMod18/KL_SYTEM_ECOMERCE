package com.marketplace.product.dto;

import com.marketplace.product.model.CondicionProductoCatalogo;
import com.marketplace.product.model.OriginalidadProducto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Vista REST de producto con atributos extendidos del caso de estudio.
 */
public record ProductoResponse(
        Long id,
        Long vendedorSolicitudId,
        String nombre,
        BigDecimal precio,
        String descripcion,
        String rutaCategoria,
        String subcategoria,
        String marca,
        OriginalidadProducto originalidad,
        String color,
        String tamano,
        Integer pesoGramos,
        String talla,
        CondicionProductoCatalogo condicion,
        Integer cantidadStock,
        String imagenesUrls,
        Instant creadoEn) {
}
