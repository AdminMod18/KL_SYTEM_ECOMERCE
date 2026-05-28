package com.marketplace.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Ajuste de inventario por el vendedor dueño del producto. */
@Getter
@Setter
public class ProductoStockUpdateRequest {

    @NotNull
    private Long vendedorSolicitudId;

    @NotNull
    @Min(0)
    private Integer cantidadStock;
}
