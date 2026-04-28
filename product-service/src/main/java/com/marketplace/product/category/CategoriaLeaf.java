package com.marketplace.product.category;

/**
 * Propósito: representar una categoría indivisible.
 * Patrón: Composite (Leaf).
 * Responsabilidad: devolver su propio nombre y ruta base.
 */
public record CategoriaLeaf(String nombre) implements CategoriaComponent {

    @Override
    public String ruta() {
        return nombre;
    }
}
