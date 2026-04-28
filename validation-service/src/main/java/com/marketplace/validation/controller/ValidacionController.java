package com.marketplace.validation.controller;

import com.marketplace.validation.dto.ValidacionRequest;
import com.marketplace.validation.dto.ValidacionResponse;
import com.marketplace.validation.facade.ValidacionCrediticiaFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Propósito: API pública de validación integral para solicitantes del marketplace.
 * Patrón: Adapter HTTP → caso de uso (entrada/salida REST sin lógica de negocio).
 * Responsabilidad: validar payload y delegar en {@link ValidacionCrediticiaFacade}.
 */
@Tag(name = "Validación", description = "Validación crediticia unificada")
@RestController
@RequestMapping
public class ValidacionController {

    private final ValidacionCrediticiaFacade validacionCrediticiaFacade;

    public ValidacionController(ValidacionCrediticiaFacade validacionCrediticiaFacade) {
        this.validacionCrediticiaFacade = validacionCrediticiaFacade;
    }

    @Operation(summary = "Validar solicitante", description = "Ejecuta la facada de validación sobre fuentes configuradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultado de validación"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content)
    })
    @PostMapping("/validar")
    public ValidacionResponse validar(
            @Parameter(name = "X-Request-Id", description = "Identificador de correlación opcional", in = ParameterIn.HEADER, required = false)
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Documento y datos del solicitante", required = true)
            @Valid @RequestBody ValidacionRequest request) {
        return validacionCrediticiaFacade.validar(request);
    }
}
