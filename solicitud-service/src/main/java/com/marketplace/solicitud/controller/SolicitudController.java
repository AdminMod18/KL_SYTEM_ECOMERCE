package com.marketplace.solicitud.controller;

import com.marketplace.solicitud.dto.CalificacionVendedorCreateRequest;
import com.marketplace.solicitud.dto.CalificacionVendedorResponse;
import com.marketplace.solicitud.dto.ReputacionResumenResponse;
import com.marketplace.solicitud.dto.ActivacionVendedorRequest;
import com.marketplace.solicitud.dto.EstadoUpdateRequest;
import com.marketplace.solicitud.dto.SolicitudCreateRequest;
import com.marketplace.solicitud.dto.SolicitudFiltrosConsulta;
import com.marketplace.solicitud.dto.SolicitudListadoItemResponse;
import com.marketplace.solicitud.dto.SolicitudResponse;
import com.marketplace.solicitud.dto.ValidacionAutomaticaRequest;
import com.marketplace.solicitud.facade.ConsultaSolicitudesDirectorFacade;
import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.service.CalificacionVendedorService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * Propósito: exponer la API REST del microservicio de solicitudes.
 * Patrón: Controller / Adapter (HTTP → casos de uso).
 * Responsabilidad: validar entrada, delegar en {@link SolicitudService} y devolver DTOs.
 */
@Tag(name = "Solicitudes", description = "Alta, consulta, listado, activación con pago y cambio de estado")
@RestController
@RequestMapping("/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final CalificacionVendedorService calificacionVendedorService;
    private final ConsultaSolicitudesDirectorFacade consultaSolicitudesDirectorFacade;

    public SolicitudController(
            SolicitudService solicitudService,
            CalificacionVendedorService calificacionVendedorService,
            ConsultaSolicitudesDirectorFacade consultaSolicitudesDirectorFacade) {
        this.solicitudService = solicitudService;
        this.calificacionVendedorService = calificacionVendedorService;
        this.consultaSolicitudesDirectorFacade = consultaSolicitudesDirectorFacade;
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

    @Operation(
            summary = "Listar solicitudes (panel director)",
            description =
                    "Filtrado opcional por documento (contiene, sin distinguir mayúsculas), estado, rango de creación y texto libre en nombre/correo/nombres/apellidos. "
                            + "Respuesta liviana sin adjuntos; use GET /{id} para detalle con adjuntos.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Listado")})
    @GetMapping
    public List<SolicitudListadoItemResponse> listar(
            @Parameter(name = "X-Request-Id", description = "Identificador de correlación opcional", in = ParameterIn.HEADER, required = false)
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @Parameter(description = "Documento de identidad (subcadena, case-insensitive)")
            @RequestParam(value = "documentoIdentidad", required = false)
                    String documentoIdentidad,
            @Parameter(description = "Estado exacto")
            @RequestParam(value = "estado", required = false)
                    SolicitudEstado estado,
            @Parameter(description = "Creado en o después de este instante (ISO-8601)")
            @RequestParam(value = "creadoDesde", required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Instant creadoDesde,
            @Parameter(description = "Creado en o antes de este instante (ISO-8601)")
            @RequestParam(value = "creadoHasta", required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Instant creadoHasta,
            @Parameter(description = "Texto en nombre comercial, correo, nombres o apellidos (subcadena)")
            @RequestParam(value = "q", required = false)
                    String q,
            @Parameter(description = "Id exacto de solicitud (radicado numérico interno)")
            @RequestParam(value = "solicitudId", required = false)
                    Long solicitudId) {
        SolicitudFiltrosConsulta filtros =
                new SolicitudFiltrosConsulta(solicitudId, documentoIdentidad, estado, creadoDesde, creadoHasta, q);
        return consultaSolicitudesDirectorFacade.listarFiltrado(filtros);
    }

    @Operation(
            summary = "Resumen de reputación del vendedor",
            description = "Promedio y total de calificaciones (1–10) para la solicitud; uso en ficha de producto (HU-17).")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description =
                            "Resumen calculado; si la solicitud no existe en esta instalación, total 0 y promedio null (tolerante a catálogo).")
    })
    @GetMapping("/{id}/reputacion-resumen")
    public ReputacionResumenResponse reputacionResumen(
            @Parameter(description = "Id de la solicitud del vendedor", required = true) @PathVariable Long id) {
        return calificacionVendedorService.resumenPorSolicitud(id);
    }

    @Operation(summary = "Obtener solicitud por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud encontrada"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public SolicitudResponse obtenerPorId(
            @Parameter(description = "Id de la solicitud", required = true) @PathVariable Long id) {
        return solicitudService.obtenerPorId(id);
    }

    @Operation(
            summary = "Actualizar estado de una solicitud",
            description =
                    "Respeta el motor de transiciones (State). No permite fijar APROBADA ni ACTIVA por PUT "
                            + "(use POST /{id}/validacion-automatica y POST /{id}/activacion-vendedor).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "409", description = "Transición no permitida o destino reservado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content)
    })
    @PutMapping("/{id}/estado")
    public SolicitudResponse actualizarEstado(
            @Parameter(description = "Id de la solicitud", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Nuevo estado", required = true)
            @Valid @RequestBody EstadoUpdateRequest request) {
        return solicitudService.cambiarEstado(id, request);
    }

    @Operation(summary = "Ejecutar validación automática de vendedor", description = "Invoca Datacrédito (mock), CIFIN (archivo) y judicial (mock); aplica política y actualiza estado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validación aplicada"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Validación no permitida en el estado actual", content = @Content)
    })
    @PostMapping("/{id}/validacion-automatica")
    public SolicitudResponse validacionAutomatica(
            @Parameter(description = "Id de la solicitud", required = true) @PathVariable Long id,
            @Valid @RequestBody ValidacionAutomaticaRequest request) {
        return solicitudService.ejecutarValidacionAutomatica(id, request);
    }

    @Operation(
            summary = "Activar vendedor tras pago",
            description =
                    "Solo en estado APROBADA. Invoca payment-service (POST /pagos); si el cobro es exitoso, pasa la solicitud a ACTIVA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud activada"),
            @ApiResponse(responseCode = "400", description = "Payload de pago inválido", content = @Content),
            @ApiResponse(responseCode = "402", description = "Pago no exitoso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "La solicitud no está en APROBADA", content = @Content),
            @ApiResponse(responseCode = "502", description = "payment-service devolvió error HTTP", content = @Content),
            @ApiResponse(responseCode = "503", description = "payment-service no configurado o no disponible", content = @Content)
    })
    @PostMapping("/{id}/activacion-vendedor")
    public SolicitudResponse activarVendedorTrasPago(
            @Parameter(description = "Id de la solicitud", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del cobro (mismo contrato que payment-service)", required = true)
            @Valid @RequestBody ActivacionVendedorRequest request) {
        return solicitudService.activarVendedorTrasPago(id, request);
    }

    @Operation(
            summary = "Renovar suscripción del vendedor",
            description =
                    "En ACTIVA extiende la fecha de vencimiento tras pago exitoso; en EN_MORA regulariza y vuelve a ACTIVA. Mismo payload que activación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suscripción renovada"),
            @ApiResponse(responseCode = "400", description = "Payload de pago inválido", content = @Content),
            @ApiResponse(responseCode = "402", description = "Pago no exitoso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Estado no permite renovación", content = @Content)
    })
    @PostMapping("/{id}/renovar-suscripcion")
    public SolicitudResponse renovarSuscripcionTrasPago(
            @Parameter(description = "Id de la solicitud", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del cobro", required = true)
            @Valid @RequestBody ActivacionVendedorRequest request) {
        return solicitudService.renovarSuscripcionTrasPago(id, request);
    }

    @Operation(
            summary = "Registrar calificación al vendedor",
            description =
                    "Demo post-compra (§6). Solo con solicitud ACTIVA o EN_MORA. La política automática evalúa malas (<3) y promedio.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Calificación registrada"),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Estado no permite calificar", content = @Content)
    })
    @PostMapping("/{id}/calificaciones-vendedor")
    @ResponseStatus(HttpStatus.CREATED)
    public CalificacionVendedorResponse registrarCalificacionVendedor(
            @Parameter(description = "Id de la solicitud del vendedor", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Nota 1–10 y metadatos opcionales", required = true)
            @Valid @RequestBody CalificacionVendedorCreateRequest request) {
        return calificacionVendedorService.registrar(id, request);
    }
}
