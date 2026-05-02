package com.marketplace.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoInteraccionRespuestaRequest {

    @NotBlank
    @Size(max = 2000)
    private String respuesta;
}
