package com.marketplace.solicitud.notification;

import com.marketplace.solicitud.model.SolicitudEstado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

/**
 * Notificación local (log) y, si hay URL configurada, publicación en notification-service (Observer remoto).
 */
@Component
public class LoggingSolicitudEstadoNotificadorMock implements SolicitudEstadoNotificador {

    private static final Logger log = LoggerFactory.getLogger(LoggingSolicitudEstadoNotificadorMock.class);

    private final RestTemplate notificationRestTemplate;

    @Value("${integracion.notification.base-url:}")
    private String notificationBaseUrl;

    public LoggingSolicitudEstadoNotificadorMock(
            @Autowired @Qualifier("notificationRestTemplate") RestTemplate notificationRestTemplate) {
        this.notificationRestTemplate = notificationRestTemplate;
    }

    @Override
    public void notificarResolucion(
            SolicitudEstado estado,
            Long solicitudId,
            String nombreVendedor,
            String correoElectronico,
            String motivoResolucion) {
        if (estado != SolicitudEstado.APROBADA
                && estado != SolicitudEstado.RECHAZADA
                && estado != SolicitudEstado.DEVUELTA) {
            return;
        }
        log.info(
                "Correo certificado (demo): estado={} id={} vendedor={} correo={} motivo={}",
                estado,
                solicitudId,
                nombreVendedor,
                correoElectronico,
                motivoResolucion);
        publicarResolucionEvaluacionSiHayIntegracion(
                estado, nombreVendedor, correoElectronico, solicitudId, motivoResolucion);
    }

    private void publicarResolucionEvaluacionSiHayIntegracion(
            SolicitudEstado estado,
            String nombreVendedor,
            String correoElectronico,
            Long solicitudId,
            String motivoResolucion) {
        if (notificationBaseUrl == null || notificationBaseUrl.isBlank()) {
            return;
        }
        String tipo =
                estado == SolicitudEstado.APROBADA
                        ? "SOLICITUD_APROBADA"
                        : estado == SolicitudEstado.RECHAZADA
                                ? "SOLICITUD_RECHAZADA"
                                : estado == SolicitudEstado.DEVUELTA ? "SOLICITUD_DEVUELTA" : null;
        if (tipo == null) {
            return;
        }
        String email =
                correoElectronico != null && !correoElectronico.isBlank()
                        ? correoElectronico.trim()
                        : "sin-email@invalid.marketplace";
        String ref =
                truncarReferencia(
                        "SOL-%d|%s"
                                .formatted(
                                        solicitudId,
                                        motivoResolucion != null && !motivoResolucion.isBlank()
                                                ? motivoResolucion
                                                : estado.name()));
        var body = new NotificacionSuscripcionPayload(tipo, email, nombreVendedor, ref);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = notificationBaseUrl.endsWith("/")
                ? notificationBaseUrl + "notificaciones/eventos"
                : notificationBaseUrl + "/notificaciones/eventos";
        try {
            notificationRestTemplate.postForEntity(url, new HttpEntity<>(body, headers), Void.class);
        } catch (RestClientException ex) {
            log.warn("No se pudo publicar evento {} en notification-service: {}", tipo, ex.getMessage());
        }
    }

    @Override
    public void notificarSuscripcion(
            SolicitudEstado estadoAlcanzado,
            Long solicitudId,
            String nombreVendedor,
            String correoElectronico,
            Instant vencimientoReferencia) {
        String ref =
                "SOL-%d|vence=%s"
                        .formatted(
                                solicitudId,
                                vencimientoReferencia != null ? vencimientoReferencia.toString() : "n/a");
        log.info(
                "Suscripción: estado {} (id={}, vendedor={}, ref={})",
                estadoAlcanzado,
                solicitudId,
                nombreVendedor,
                ref);
        publicarSiHayIntegracion(estadoAlcanzado, nombreVendedor, correoElectronico, ref);
    }

    @Override
    public void notificarCancelacionPorReputacion(
            Long solicitudId,
            String nombreVendedor,
            String correoElectronico,
            String detalleMotivo) {
        String ref =
                truncarReferencia(
                        "SOL-%d|%s".formatted(solicitudId, detalleMotivo != null ? detalleMotivo : ""));
        log.info(
                "Reputación §6: solicitud CANCELADA (id={}, vendedor={}, detalle={})",
                solicitudId,
                nombreVendedor,
                detalleMotivo);
        publicarTipoSiHayIntegracion(
                "SOLICITUD_CANCELACION_REPUTACION", nombreVendedor, correoElectronico, ref);
    }

    private void publicarSiHayIntegracion(
            SolicitudEstado estadoAlcanzado, String nombreVendedor, String correoElectronico, String referencia) {
        if (notificationBaseUrl == null || notificationBaseUrl.isBlank()) {
            return;
        }
        String tipo =
                estadoAlcanzado == SolicitudEstado.EN_MORA
                        ? "SOLICITUD_MORA"
                        : estadoAlcanzado == SolicitudEstado.CANCELADA
                                ? "SOLICITUD_CANCELACION_SUSCRIPCION"
                                : null;
        if (tipo == null) {
            return;
        }
        publicarTipoSiHayIntegracion(tipo, nombreVendedor, correoElectronico, referencia);
    }

    private void publicarTipoSiHayIntegracion(
            String tipoEvento, String nombreVendedor, String correoElectronico, String referencia) {
        if (notificationBaseUrl == null || notificationBaseUrl.isBlank()) {
            return;
        }
        String email =
                correoElectronico != null && !correoElectronico.isBlank()
                        ? correoElectronico.trim()
                        : "sin-email@invalid.marketplace";
        String url = notificationBaseUrl.endsWith("/")
                ? notificationBaseUrl + "notificaciones/eventos"
                : notificationBaseUrl + "/notificaciones/eventos";
        String refNorm = truncarReferencia(referencia);
        var body = new NotificacionSuscripcionPayload(tipoEvento, email, nombreVendedor, refNorm);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            notificationRestTemplate.postForEntity(url, new HttpEntity<>(body, headers), Void.class);
        } catch (RestClientException ex) {
            log.warn(
                    "No se pudo publicar evento {} en notification-service: {}", tipoEvento, ex.getMessage());
        }
    }

    private static String truncarReferencia(String referencia) {
        if (referencia == null || referencia.isEmpty()) {
            return "-";
        }
        return referencia.length() > 120 ? referencia.substring(0, 117) + "…" : referencia;
    }
}
