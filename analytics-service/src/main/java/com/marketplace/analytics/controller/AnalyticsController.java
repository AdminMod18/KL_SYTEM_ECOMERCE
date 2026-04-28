package com.marketplace.analytics.controller;

import com.marketplace.analytics.dto.EventoMetricaRequest;
import com.marketplace.analytics.dto.EventoMetricaResponse;
import com.marketplace.analytics.dto.KpiResponse;
import com.marketplace.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Propósito: API de ingesta de eventos y consulta de KPIs.
 * Patrón: Adapter HTTP.
 * Responsabilidad: validar entrada y delegar en {@link AnalyticsService}.
 */
@Tag(name = "Analítica", description = "Ingesta de eventos y KPIs")
@RestController
@RequestMapping
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Operation(summary = "Registrar evento de métrica")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento almacenado"),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content)
    })
    @PostMapping("/eventos")
    @ResponseStatus(HttpStatus.CREATED)
    public EventoMetricaResponse registrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Tipo y magnitud del evento", required = true)
            @Valid @RequestBody EventoMetricaRequest request) {
        return analyticsService.registrarEvento(request);
    }

    @Operation(summary = "Consultar KPIs agregados")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Indicadores actuales")})
    @GetMapping("/kpis")
    public KpiResponse kpis(
            @Parameter(name = "X-Request-Id", description = "Identificador de correlación opcional", in = ParameterIn.HEADER, required = false)
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        return analyticsService.obtenerKpis();
    }
}
