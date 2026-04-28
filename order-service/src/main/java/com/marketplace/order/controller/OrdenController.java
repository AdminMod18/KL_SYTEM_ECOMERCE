package com.marketplace.order.controller;

import com.marketplace.order.dto.OrdenCreateRequest;
import com.marketplace.order.dto.OrdenResponse;
import com.marketplace.order.service.OrdenApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Propósito: API HTTP para creación de órdenes de compra.
 * Patrón: Adapter de entrada (REST → servicio de aplicación).
 * Responsabilidad: validar el cuerpo y delegar en {@link OrdenApplicationService}.
 */
@Tag(name = "Órdenes", description = "Creación de órdenes de compra")
@RestController
@RequestMapping
public class OrdenController {

    private final OrdenApplicationService ordenApplicationService;

    public OrdenController(OrdenApplicationService ordenApplicationService) {
        this.ordenApplicationService = ordenApplicationService;
    }

    @Operation(summary = "Crear orden", description = "Persiste la orden y calcula totales con la cadena de precios.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orden creada"),
            @ApiResponse(responseCode = "400", description = "Validación o reglas de negocio", content = @Content)
    })
    @PostMapping("/orden")
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenResponse crearOrden(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Líneas y datos de la orden", required = true)
            @Valid @RequestBody OrdenCreateRequest solicitud) {
        return ordenApplicationService.colocarOrden(solicitud);
    }
}
