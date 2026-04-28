package com.marketplace.product.builder;

import com.marketplace.product.entity.Producto;

import java.math.BigDecimal;

/**
 * Propósito: definir construcción paso a paso de la entidad Producto.
 * Patrón: Builder.
 * Responsabilidad: separar armado del objeto de la lógica de uso en el servicio.
 */
public interface ProductoBuilder {

    ProductoBuilder conNombre(String nombre);

    ProductoBuilder conPrecio(BigDecimal precio);

    ProductoBuilder conDescripcion(String descripcion);

    ProductoBuilder conRutaCategoria(String rutaCategoria);

    Producto construir();
}
