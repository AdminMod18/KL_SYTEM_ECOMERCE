package com.marketplace.solicitud.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Entrada para registrar una calificación post-compra (demo).
 */
@Getter
@Setter
public class CalificacionVendedorCreateRequest {

    @Schema(description = "Nota de 1 a 10", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(1)
    @Max(10)
    private int valor;

    @Size(max = 500)
    private String comentario;

    @Size(max = 64)
    private String referenciaOrden;
}
