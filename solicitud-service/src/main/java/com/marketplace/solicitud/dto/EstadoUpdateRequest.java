package com.marketplace.solicitud.dto;

import com.marketplace.solicitud.model.SolicitudEstado;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Propósito: carga útil para cambiar el estado de una solicitud vía API.
 * Patrón: DTO (Data Transfer Object).
 * Responsabilidad: representar el estado destino validado antes de delegar al motor de transiciones.
 */
@Getter
@Setter
public class EstadoUpdateRequest {

    @NotNull
    private SolicitudEstado estado;
}
