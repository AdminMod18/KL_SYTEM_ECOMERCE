package com.marketplace.payment.controller;

import com.marketplace.payment.dto.PagoRequest;
import com.marketplace.payment.dto.PagoResponse;
import com.marketplace.payment.service.PagoService;
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
 * Propósito: exponer la API HTTP de cobros unificada (`POST /pagos`).
 * Patrón: Adapter de entrada (HTTP → servicio de aplicación).
 * Responsabilidad: validar el DTO y delegar el procesamiento al {@link PagoService}.
 */
@Tag(name = "Pagos", description = "Procesamiento de cobros")
@RestController
@RequestMapping
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @Operation(summary = "Crear pago", description = "Selecciona estrategia según método y procesa el cobro.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago registrado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content)
    })
    @PostMapping("/pagos")
    @ResponseStatus(HttpStatus.CREATED)
    public PagoResponse crearPago(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Monto, moneda y método de pago", required = true)
            @Valid @RequestBody PagoRequest request) {
        return pagoService.procesarPago(request);
    }
}
