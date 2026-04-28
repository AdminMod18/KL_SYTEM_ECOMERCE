package com.marketplace.product.category;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Propósito: componer múltiples categorías para obtener una ruta jerárquica.
 * Patrón: Composite (Composite).
 * Responsabilidad: agrupar nodos hijos y construir una ruta normalizada con separador '/'.
 */
public class CategoriaComposite implements CategoriaComponent {

    private final String nombre;
    private final List<CategoriaComponent> hijos = new ArrayList<>();

    public CategoriaComposite(String nombre) {
        this.nombre = nombre;
    }

    public CategoriaComposite agregar(CategoriaComponent hijo) {
        hijos.add(hijo);
        return this;
    }

    @Override
    public String nombre() {
        return nombre;
    }

    @Override
    public String ruta() {
        if (hijos.isEmpty()) {
            return nombre;
        }
        return nombre + "/" + hijos.stream().map(CategoriaComponent::ruta).collect(Collectors.joining("/"));
    }
}
