package com.marketplace.product.category;

/**
 * Propósito: contrato uniforme para nodos de categoría simples o compuestas.
 * Patrón: Composite (Component).
 * Responsabilidad: exponer nombre y ruta jerárquica consolidada.
 */
public interface CategoriaComponent {

    String nombre();

    String ruta();
}
