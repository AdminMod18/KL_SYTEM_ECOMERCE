package com.marketplace.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Propósito: línea de pedido en la solicitud REST de creación de orden.
 * Patrón: DTO de entrada.
 * Responsabilidad: describir SKU, cantidad y precio unitario acordado en el carrito.
 */
@Getter
@Setter
public class LineaOrdenRequest {

    @NotBlank
    @Size(max = 64)
    private String sku;

    @NotNull
    @Min(1)
    private Integer cantidad;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal precioUnitario;
}
