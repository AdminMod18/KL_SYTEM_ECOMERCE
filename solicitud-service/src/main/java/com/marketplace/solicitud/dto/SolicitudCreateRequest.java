package com.marketplace.solicitud.dto;

import com.marketplace.solicitud.model.TipoPersonaSolicitante;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Propósito: entrada REST para crear una solicitud de vendedor (datos personales y adjuntos).
 * Patrón: DTO (Data Transfer Object) + validación declarativa.
 * Responsabilidad: transportar datos del cliente sin exponer la entidad JPA.
 */
@Getter
@Setter
public class SolicitudCreateRequest {

    /**
     * Opcional: si se envía, tiene prioridad como nombre mostrado sobre {@code nombres + apellidos}.
     */
    @Size(max = 200)
    private String nombreVendedor;

    @NotBlank
    @Size(max = 120)
    private String nombres;

    @NotBlank
    @Size(max = 120)
    private String apellidos;

    @NotBlank
    @Size(min = 5, max = 32)
    private String documentoIdentidad;

    @NotBlank
    @Email
    @Size(max = 320)
    private String correoElectronico;

    @NotBlank
    @Size(max = 120)
    private String paisResidencia;

    @NotBlank
    @Size(max = 120)
    private String ciudadResidencia;

    @NotBlank
    @Size(max = 40)
    private String telefono;

    @NotNull
    private TipoPersonaSolicitante tipoPersona;

    @NotEmpty
    @Valid
    private List<AdjuntoCreateDto> adjuntos = new ArrayList<>();
}
