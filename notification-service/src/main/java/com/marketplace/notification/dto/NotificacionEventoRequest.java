package com.marketplace.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Proposito: entrada REST para publicar eventos que requieren notificacion.
 * Patron: DTO de entrada.
 * Responsabilidad: transportar metadatos minimos para observers.
 */
@Getter
@Setter
public class NotificacionEventoRequest {

    @NotNull
    private TipoEventoNotificacion tipoEvento;

    @NotBlank
    @Email
    private String emailDestino;

    @NotBlank
    @Size(max = 120)
    private String nombreActor;

    @NotBlank
    @Size(max = 120)
    private String referencia;
}
