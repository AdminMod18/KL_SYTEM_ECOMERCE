package com.marketplace.solicitud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Propósito: entrada REST para crear una solicitud de vendedor.
 * Patrón: DTO (Data Transfer Object).
 * Responsabilidad: transportar y validar datos del cliente sin exponer la entidad JPA.
 */
@Getter
@Setter
public class SolicitudCreateRequest {

    @NotBlank
    @Size(max = 200)
    private String nombreVendedor;

    @NotBlank
    @Size(min = 5, max = 32)
    private String documentoIdentidad;
}
