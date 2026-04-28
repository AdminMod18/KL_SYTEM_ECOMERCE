package com.marketplace.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Propósito: comando REST para crear productos.
 * Patrón: DTO de entrada.
 * Responsabilidad: transportar datos y ruta de categorías para construir el producto.
 */
@Getter
@Setter
public class ProductoCreateRequest {

    @NotBlank
    @Size(max = 200)
    private String nombre;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal precio;

    @NotBlank
    @Size(max = 500)
    private String descripcion;

    @NotNull
    private List<@NotBlank String> categorias;
}
