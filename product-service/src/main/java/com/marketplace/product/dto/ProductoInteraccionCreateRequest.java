package com.marketplace.product.dto;

import com.marketplace.product.model.ProductoInteraccionTipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoInteraccionCreateRequest {

    @NotNull
    private ProductoInteraccionTipo tipo;

    @NotBlank
    @Size(max = 2000)
    private String contenido;

    @Size(max = 120)
    private String autorNombre;
}
