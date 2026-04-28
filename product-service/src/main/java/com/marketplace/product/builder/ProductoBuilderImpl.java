package com.marketplace.product.builder;

import com.marketplace.product.entity.Producto;

import java.math.BigDecimal;

/**
 * Propósito: implementación concreta del builder de productos.
 * Patrón: Builder concreto.
 * Responsabilidad: mantener estado temporal de construcción y producir una entidad lista.
 */
public class ProductoBuilderImpl implements ProductoBuilder {

    private String nombre;
    private BigDecimal precio;
    private String descripcion;
    private String rutaCategoria;

    @Override
    public ProductoBuilder conNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    @Override
    public ProductoBuilder conPrecio(BigDecimal precio) {
        this.precio = precio;
        return this;
    }

    @Override
    public ProductoBuilder conDescripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }

    @Override
    public ProductoBuilder conRutaCategoria(String rutaCategoria) {
        this.rutaCategoria = rutaCategoria;
        return this;
    }

    @Override
    public Producto construir() {
        Producto resultado = new Producto();
        resultado.setNombre(nombre);
        resultado.setPrecio(precio);
        resultado.setDescripcion(descripcion);
        resultado.setRutaCategoria(rutaCategoria);
        this.nombre = null;
        this.precio = null;
        this.descripcion = null;
        this.rutaCategoria = null;
        return resultado;
    }
}
