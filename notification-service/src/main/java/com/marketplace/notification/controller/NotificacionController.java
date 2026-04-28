package com.marketplace.notification.controller;

import com.marketplace.notification.dto.NotificacionEventoRequest;
import com.marketplace.notification.dto.NotificacionEventoResponse;
import com.marketplace.notification.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Proposito: API REST para recibir eventos y ejecutar notificaciones observables.
 * Patron: Adapter HTTP.
 * Responsabilidad: exponer POST /notificaciones/eventos y delegar en el servicio.
 */
@Tag(name = "Notificaciones", description = "Eventos de dominio y canales de notificación")
@RestController
@RequestMapping
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @Operation(summary = "Publicar evento", description = "Dispara notificaciones según el tipo de evento recibido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento procesado"),
            @ApiResponse(responseCode = "400", description = "Payload inválido", content = @Content)
    })
    @PostMapping("/notificaciones/eventos")
    public NotificacionEventoResponse publicarEvento(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Tipo y payload del evento", required = true)
            @Valid @RequestBody NotificacionEventoRequest request) {
        return notificacionService.procesar(request);
    }
}
