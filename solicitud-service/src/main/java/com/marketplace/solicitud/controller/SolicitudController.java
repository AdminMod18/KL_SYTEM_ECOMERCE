package com.marketplace.solicitud.controller;

import com.marketplace.solicitud.dto.EstadoUpdateRequest;
import com.marketplace.solicitud.dto.SolicitudCreateRequest;
import com.marketplace.solicitud.dto.SolicitudResponse;
import com.marketplace.solicitud.service.SolicitudService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Propósito: exponer la API REST del microservicio de solicitudes.
 * Patrón: Controller / Adapter (HTTP → casos de uso).
 * Responsabilidad: validar entrada, delegar en {@link SolicitudService} y devolver DTOs.
 */
@Tag(name = "Solicitudes", description = "Alta, listado y cambio de estado de solicitudes")
@RestController
@RequestMapping("/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @Operation(summary = "Crear solicitud")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Solicitud creada"),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudResponse crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la solicitud", required = true)
            @Valid @RequestBody SolicitudCreateRequest request) {
        return solicitudService.crear(request);
    }

    @Operation(summary = "Listar solicitudes")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Listado")})
    @GetMapping
    public List<SolicitudResponse> listar(
            @Parameter(name = "X-Request-Id", description = "Identificador de correlación opcional", in = ParameterIn.HEADER, required = false)
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        return solicitudService.listar();
    }

    @Operation(summary = "Actualizar estado de una solicitud")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Transición no permitida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content)
    })
    @PutMapping("/{id}/estado")
    public SolicitudResponse actualizarEstado(
            @Parameter(description = "Id de la solicitud", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Nuevo estado", required = true)
            @Valid @RequestBody EstadoUpdateRequest request) {
        return solicitudService.cambiarEstado(id, request);
    }
}
