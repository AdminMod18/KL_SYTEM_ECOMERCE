package com.marketplace.solicitud.service;

import com.marketplace.solicitud.chain.SolicitudValidationHandler;
import com.marketplace.solicitud.command.ValidacionCreacionSolicitudCommand;
import com.marketplace.solicitud.config.SuscripcionProperties;
import com.marketplace.solicitud.dto.ActivacionVendedorRequest;
import com.marketplace.solicitud.dto.AdjuntoCreateDto;
import com.marketplace.solicitud.dto.AdjuntoResponse;
import com.marketplace.solicitud.dto.EstadoUpdateRequest;
import com.marketplace.solicitud.dto.SolicitudCreateRequest;
import com.marketplace.solicitud.dto.SolicitudResponse;
import com.marketplace.solicitud.dto.ValidacionAutomaticaRequest;
import com.marketplace.solicitud.exception.SolicitudBusinessException;
import com.marketplace.solicitud.entity.Solicitud;
import com.marketplace.solicitud.entity.SolicitudAdjunto;
import com.marketplace.solicitud.factory.SolicitudAdjuntoFactory;
import com.marketplace.solicitud.integration.ValidacionVendedorResult;
import com.marketplace.solicitud.integration.PagoGateway;
import com.marketplace.solicitud.integration.UserServicePromoverVendedorClient;
import com.marketplace.solicitud.integration.PagoRemotoResult;
import com.marketplace.solicitud.integration.ValidacionVendedorGateway;
import com.marketplace.solicitud.notification.SolicitudEstadoNotificador;
import com.marketplace.solicitud.model.PeriodoSuscripcionPlan;
import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.repository.SolicitudRepository;
import com.marketplace.solicitud.state.SolicitudEstadoRegistry;
import com.marketplace.solicitud.storage.AdjuntosSolicitudFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Propósito: casos de uso de solicitudes (crear, consultar por id, validación, activación, cambio de estado).
 * Patrón: Application Service (capa de aplicación); orquesta Chain of Responsibility y State.
 * Responsabilidad: transacciones, mapeo a DTOs y delegación en cadena de validación y registro de estados.
 */
@Service
public class SolicitudService {

    private static final Logger log = LoggerFactory.getLogger(SolicitudService.class);

    private final SolicitudRepository solicitudRepository;
    private final SolicitudValidationHandler cadenaValidacionCreacion;
    private final SolicitudEstadoRegistry estadoRegistry;
    private final ValidacionVendedorGateway validacionVendedorGateway;
    private final SolicitudEstadoNotificador solicitudEstadoNotificador;
    private final PagoGateway pagoGateway;
    private final AdjuntosSolicitudFacade adjuntosSolicitudFacade;
    private final Clock clock;
    private final SuscripcionProperties suscripcionProperties;
    private final UserServicePromoverVendedorClient userServicePromoverVendedorClient;

    private static final Pattern CUATRO_DIGITOS = Pattern.compile("^\\d{4}$");

    private static String previewCifin(String contenidoCifin) {
        if (contenidoCifin == null || contenidoCifin.isEmpty()) {
            return "";
        }
        return contenidoCifin.length() > 120 ? contenidoCifin.substring(0, 120) + "…" : contenidoCifin;
    }

    /**
     * Acepta archivo CIFIN completo o par documento+score alineado al contrato UI (demo).
     */
    private String resolverContenidoCifin(Solicitud solicitud, ValidacionAutomaticaRequest request) {
        String raw = request.getContenidoArchivoCifin();
        if (raw != null && !raw.isBlank()) {
            return raw.trim();
        }
        if (request.getDocumento() == null || request.getDocumento().isBlank()) {
            log.warn(
                    "validacion-automatica 400: falta documento (solicitud id={}, tiene contenidoArchivoCifin={})",
                    solicitud.getId(),
                    raw != null && !raw.isBlank());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Envíe contenidoArchivoCifin o documento y score.");
        }
        if (request.getScore() == null) {
            log.warn("validacion-automatica 400: falta score (solicitud id={})", solicitud.getId());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "score es obligatorio cuando no envía contenidoArchivoCifin.");
        }
        String docReq = request.getDocumento().trim();
        String docBd = solicitud.getDocumentoIdentidad().trim();
        if (!docReq.equalsIgnoreCase(docBd)) {
            log.warn(
                    "validacion-automatica 400: documento del body no coincide con la solicitud id={} (normalizado enviado='{}' bd='{}')",
                    solicitud.getId(),
                    docReq,
                    docBd);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "documento no coincide con la solicitud.");
        }
        return docReq + "|" + request.getScore() + "|NORMAL";
    }

    public SolicitudService(
            SolicitudRepository solicitudRepository,
            SolicitudValidationHandler cadenaValidacionCreacion,
            SolicitudEstadoRegistry estadoRegistry,
            ValidacionVendedorGateway validacionVendedorGateway,
            SolicitudEstadoNotificador solicitudEstadoNotificador,
            PagoGateway pagoGateway,
            AdjuntosSolicitudFacade adjuntosSolicitudFacade,
            Clock clock,
            SuscripcionProperties suscripcionProperties,
            UserServicePromoverVendedorClient userServicePromoverVendedorClient) {
        this.solicitudRepository = solicitudRepository;
        this.cadenaValidacionCreacion = cadenaValidacionCreacion;
        this.estadoRegistry = estadoRegistry;
        this.validacionVendedorGateway = validacionVendedorGateway;
        this.solicitudEstadoNotificador = solicitudEstadoNotificador;
        this.pagoGateway = pagoGateway;
        this.adjuntosSolicitudFacade = adjuntosSolicitudFacade;
        this.clock = clock;
        this.suscripcionProperties = suscripcionProperties;
        this.userServicePromoverVendedorClient = userServicePromoverVendedorClient;
    }

    @Transactional
    public SolicitudResponse crear(SolicitudCreateRequest request) {
        normalizar(request);
        new ValidacionCreacionSolicitudCommand(cadenaValidacionCreacion, request).ejecutar();

        Solicitud solicitud = new Solicitud();
        solicitud.setNombres(request.getNombres());
        solicitud.setApellidos(request.getApellidos());
        solicitud.setCorreoElectronico(request.getCorreoElectronico());
        solicitud.setPaisResidencia(request.getPaisResidencia());
        solicitud.setCiudadResidencia(request.getCiudadResidencia());
        solicitud.setTelefono(request.getTelefono());
        solicitud.setTipoPersona(request.getTipoPersona());
        solicitud.setDocumentoIdentidad(request.getDocumentoIdentidad());
        solicitud.setNombreVendedor(deriveNombreMostrar(request));
        solicitud.setEstado(SolicitudEstado.PENDIENTE);

        for (AdjuntoCreateDto dto : request.getAdjuntos()) {
            SolicitudAdjunto adj = SolicitudAdjuntoFactory.crear(solicitud, dto);
            solicitud.getAdjuntos().add(adj);
        }

        Solicitud guardada = solicitudRepository.save(solicitud);

        List<AdjuntoCreateDto> dtos = request.getAdjuntos();
        List<SolicitudAdjunto> persistidos = guardada.getAdjuntos();
        for (int i = 0; i < dtos.size(); i++) {
            String uri = adjuntosSolicitudFacade.persistirSiHayContenido(
                    guardada.getId(), dtos.get(i).getNombreArchivo(), dtos.get(i).getContenidoBase64());
            if (uri != null) {
                persistidos.get(i).setUriArchivo(uri);
            }
        }
        guardada = solicitudRepository.save(guardada);

        log.info(
                "Solicitud creada id={} estado=PENDIENTE documento={} tipoPersona={}",
                guardada.getId(),
                guardada.getDocumentoIdentidad(),
                guardada.getTipoPersona());
        return aRespuesta(guardada);
    }

    /**
     * Ejecuta validación automática (Datacrédito mock REST + CIFIN archivo + judicial mock) y actualiza el estado según política.
     */
    @Transactional
    public SolicitudResponse ejecutarValidacionAutomatica(Long id, ValidacionAutomaticaRequest request) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        var comportamientoActual = estadoRegistry.comportamientoPara(solicitud.getEstado());
        if (!comportamientoActual.permiteValidacionAutomatica()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Validación automática no permitida en este estado. Estado actual: " + solicitud.getEstado());
        }

        log.info("Ejecutando validación automática solicitud id={} documento={}", id, solicitud.getDocumentoIdentidad());

        String contenidoCifin = resolverContenidoCifin(solicitud, request);
        log.info("Contenido CIFIN derivado para validation-service (preview): {}", previewCifin(contenidoCifin));

        ValidacionVendedorResult resultado = validacionVendedorGateway.ejecutarValidacion(
                solicitud.getDocumentoIdentidad(),
                solicitud.getNombreVendedor(),
                contenidoCifin,
                request.getExigenciaJudicial());

        SolicitudEstado propuesto = resultado.estado();
        log.info("Estado devuelto por validation-service (persistido si transición válida): {}", propuesto);

        SolicitudEstado anterior = solicitud.getEstado();
        comportamientoActual.assertTransicionPermitida(propuesto);
        solicitud.setEstado(propuesto);
        Solicitud guardada = solicitudRepository.save(solicitud);

        log.info("Estado final persistido solicitud id={} → {}", id, guardada.getEstado());
        notificarSiResolucion(anterior, guardada, resultado.detalleNotificacion());
        return aRespuesta(guardada);
    }

    /**
     * Orquesta cobro en payment-service y activa al vendedor. Solo permitido en {@link SolicitudEstado#APROBADA}.
     */
    @Transactional
    public SolicitudResponse activarVendedorTrasPago(Long id, ActivacionVendedorRequest request) {
        validarPayloadPagoActivacion(request);

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        if (solicitud.getEstado() != SolicitudEstado.APROBADA) {
            throw new SolicitudBusinessException(
                    "ACTIVACION_REQUIERE_APROBADA",
                    "Solo solicitudes en estado APROBADA pueden activarse mediante pago. Estado actual: "
                            + solicitud.getEstado());
        }

        PagoRemotoResult pago = pagoGateway.procesarPago(request, id);
        if (!pagoGateway.esPagoExitoso(pago.estado())) {
            throw new ResponseStatusException(
                    HttpStatus.PAYMENT_REQUIRED,
                    "El pago no culminó en un estado exitoso: " + pago.estado());
        }

        log.info(
                "Pago exitoso solicitud id={} idTransaccion={} estadoPago={} → activando vendedor",
                id,
                pago.idTransaccion(),
                pago.estado());

        var comportamiento = estadoRegistry.comportamientoPara(solicitud.getEstado());
        comportamiento.assertTransicionPermitida(SolicitudEstado.ACTIVA);
        solicitud.setEstado(SolicitudEstado.ACTIVA);
        solicitud.setProximoVencimientoSuscripcion(
                clock.instant().plus(suscripcionProperties.getPeriodoRenovacion()));
        Solicitud guardada = solicitudRepository.save(solicitud);
        log.info("Solicitud id={} en estado ACTIVA (pago confirmado)", id);
        userServicePromoverVendedorClient.notificarActivacionSiHayUsuarioRegistrado(
                guardada.getDocumentoIdentidad(), guardada.getCorreoElectronico());
        return aRespuesta(guardada);
    }

    /**
     * Renueva la suscripción con un nuevo cobro. Desde {@link SolicitudEstado#ACTIVA} extiende el vencimiento;
     * desde {@link SolicitudEstado#EN_MORA} regulariza y vuelve a {@link SolicitudEstado#ACTIVA}.
     */
    @Transactional
    public SolicitudResponse renovarSuscripcionTrasPago(Long id, ActivacionVendedorRequest request) {
        validarPayloadPagoActivacion(request);

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        SolicitudEstado estado = solicitud.getEstado();
        if (estado != SolicitudEstado.ACTIVA && estado != SolicitudEstado.EN_MORA) {
            throw new SolicitudBusinessException(
                    "RENOVACION_ESTADO_INVALIDO",
                    "Solo solicitudes ACTIVA o EN_MORA pueden renovar suscripción. Estado actual: " + estado);
        }

        PagoRemotoResult pago = pagoGateway.procesarPago(request, id);
        if (!pagoGateway.esPagoExitoso(pago.estado())) {
            throw new ResponseStatusException(
                    HttpStatus.PAYMENT_REQUIRED,
                    "El pago no culminó en un estado exitoso: " + pago.estado());
        }

        var comportamiento = estadoRegistry.comportamientoPara(solicitud.getEstado());
        if (estado == SolicitudEstado.EN_MORA) {
            comportamiento.assertTransicionPermitida(SolicitudEstado.ACTIVA);
            solicitud.setEstado(SolicitudEstado.ACTIVA);
            solicitud.setEntradaEnMoraEn(null);
        }
        solicitud.setProximoVencimientoSuscripcion(
                clock.instant().plus(resolverDuracionSuscripcion(request)));
        Solicitud guardada = solicitudRepository.save(solicitud);
        log.info("Suscripción renovada solicitud id={} estado={}", id, guardada.getEstado());
        return aRespuesta(guardada);
    }

    private Duration resolverDuracionSuscripcion(ActivacionVendedorRequest request) {
        PeriodoSuscripcionPlan plan = request.getPeriodoSuscripcion();
        if (plan != null) {
            return plan.getDuracion();
        }
        return suscripcionProperties.getPeriodoRenovacion();
    }

    private void validarPayloadPagoActivacion(ActivacionVendedorRequest request) {
        switch (request.getTipo()) {
            case ONLINE -> {
                if (request.getTokenPasarela() == null || request.getTokenPasarela().isBlank()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "tokenPasarela es obligatorio para tipo ONLINE.");
                }
            }
            case TARJETA -> {
                String u = request.getUltimosDigitosTarjeta();
                if (u == null || !CUATRO_DIGITOS.matcher(u).matches()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "ultimosDigitosTarjeta debe tener exactamente 4 dígitos.");
                }
            }
            case CONSIGNACION -> {
                if (request.getNumeroComprobanteConsignacion() == null
                        || request.getNumeroComprobanteConsignacion().isBlank()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "numeroComprobanteConsignacion es obligatorio para tipo CONSIGNACION.");
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public SolicitudResponse obtenerPorId(Long id) {
        return solicitudRepository
                .findWithAdjuntosById(id)
                .map(this::aRespuesta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
    }

    @Transactional
    public SolicitudResponse cambiarEstado(Long id, EstadoUpdateRequest request) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        SolicitudEstado destino = request.getEstado();
        if (destino == SolicitudEstado.APROBADA || destino == SolicitudEstado.ACTIVA) {
            throw new SolicitudBusinessException(
                    "TRANSICION_PUT_NO_PERMITIDA",
                    "No se puede fijar APROBADA ni ACTIVA mediante PUT /estado. Use POST /{id}/validacion-automatica "
                            + "para cerrar evaluación y POST /{id}/activacion-vendedor (tras pago exitoso) para ACTIVA.");
        }

        SolicitudEstado anterior = solicitud.getEstado();
        estadoRegistry.comportamientoPara(solicitud.getEstado()).assertTransicionPermitida(destino);

        solicitud.setEstado(destino);
        Solicitud guardada = solicitudRepository.save(solicitud);
        notificarSiResolucion(anterior, guardada, null);
        return aRespuesta(guardada);
    }

    private static void normalizar(SolicitudCreateRequest request) {
        if (request.getNombres() != null) {
            request.setNombres(request.getNombres().trim());
        }
        if (request.getApellidos() != null) {
            request.setApellidos(request.getApellidos().trim());
        }
        if (request.getDocumentoIdentidad() != null) {
            request.setDocumentoIdentidad(request.getDocumentoIdentidad().trim());
        }
        if (request.getCorreoElectronico() != null) {
            request.setCorreoElectronico(request.getCorreoElectronico().trim());
        }
        if (request.getPaisResidencia() != null) {
            request.setPaisResidencia(request.getPaisResidencia().trim());
        }
        if (request.getCiudadResidencia() != null) {
            request.setCiudadResidencia(request.getCiudadResidencia().trim());
        }
        if (request.getTelefono() != null) {
            request.setTelefono(request.getTelefono().trim());
        }
        if (request.getNombreVendedor() != null) {
            request.setNombreVendedor(request.getNombreVendedor().trim());
        }
        if (request.getAdjuntos() != null) {
            for (AdjuntoCreateDto a : request.getAdjuntos()) {
                if (a.getNombreArchivo() != null) {
                    a.setNombreArchivo(a.getNombreArchivo().trim());
                }
            }
        }
    }

    private static String deriveNombreMostrar(SolicitudCreateRequest request) {
        if (request.getNombreVendedor() != null && !request.getNombreVendedor().isBlank()) {
            return request.getNombreVendedor();
        }
        return (request.getNombres() + " " + request.getApellidos()).trim();
    }

    private SolicitudResponse aRespuesta(Solicitud solicitud) {
        List<AdjuntoResponse> adjuntos =
                solicitud.getAdjuntos() == null
                        ? List.of()
                        : solicitud.getAdjuntos().stream()
                                .map(a -> new AdjuntoResponse(a.getTipo(), a.getNombreArchivo(), a.getUriArchivo()))
                                .toList();
        return new SolicitudResponse(
                solicitud.getId(),
                solicitud.getNumeroRadicado(),
                solicitud.getNombreVendedor(),
                solicitud.getNombres(),
                solicitud.getApellidos(),
                solicitud.getDocumentoIdentidad(),
                solicitud.getCorreoElectronico(),
                solicitud.getPaisResidencia(),
                solicitud.getCiudadResidencia(),
                solicitud.getTelefono(),
                solicitud.getTipoPersona(),
                adjuntos,
                solicitud.getEstado(),
                solicitud.getCreadoEn(),
                solicitud.getProximoVencimientoSuscripcion());
    }

    /**
     * Mock de notificación al cerrar evaluación (APROBADA / RECHAZADA), solo si el estado cambió.
     */
    private void notificarSiResolucion(SolicitudEstado anterior, Solicitud guardada, String motivoResolucion) {
        SolicitudEstado nuevo = guardada.getEstado();
        if (anterior == nuevo) {
            return;
        }
        if (nuevo == SolicitudEstado.APROBADA
                || nuevo == SolicitudEstado.RECHAZADA
                || nuevo == SolicitudEstado.DEVUELTA) {
            solicitudEstadoNotificador.notificarResolucion(
                    nuevo,
                    guardada.getId(),
                    guardada.getNombreVendedor(),
                    guardada.getCorreoElectronico(),
                    motivoResolucion);
        }
    }
}
