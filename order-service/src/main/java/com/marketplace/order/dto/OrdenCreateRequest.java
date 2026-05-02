package com.marketplace.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Propósito: comando HTTP para crear una orden con una o más líneas.
 * Patrón: DTO / comando de aplicación.
 * Responsabilidad: transportar identificador de cliente y líneas validadas.
 */
@Getter
@Setter
public class OrdenCreateRequest {

    @NotBlank
    @Size(max = 64)
    private String clienteId;

    /** Opcional: si falta, el envío usa tarifa local por defecto (config). */
    @Size(max = 120)
    private String paisEnvio;

    @Size(max = 120)
    private String ciudadEnvio;

    @Size(max = 240)
    private String direccionEnvio;

    /** {@code RECOGIDA} o {@code DOMICILIO} (HU-20). Por defecto DOMICILIO en servidor si viene vacío. */
    @Size(max = 24)
    private String tipoEntrega;

    @NotEmpty
    @Valid
    private List<LineaOrdenRequest> lineas;
}
