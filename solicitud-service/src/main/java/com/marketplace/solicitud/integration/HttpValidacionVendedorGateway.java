package com.marketplace.solicitud.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.marketplace.solicitud.model.SolicitudEstado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cliente HTTP hacia {@code POST {base-url}/validar} del validation-service.
 * La base URL se normaliza (sin barra final) para evitar {@code //validar}.
 */
@Component
public class HttpValidacionVendedorGateway implements ValidacionVendedorGateway {

    private static final Logger log = LoggerFactory.getLogger(HttpValidacionVendedorGateway.class);

    private final RestTemplate restTemplate;
    private final String validationBaseUrl;

    public HttpValidacionVendedorGateway(
            @Qualifier("validationRestTemplate") RestTemplate restTemplate,
            @Value("${integracion.validation.base-url:}") String validationBaseUrl) {
        this.restTemplate = restTemplate;
        this.validationBaseUrl = validationBaseUrl == null ? "" : validationBaseUrl.trim();
    }

    @Override
    public ValidacionVendedorResult ejecutarValidacion(
            String documentoIdentidad,
            String nombreVendedor,
            String contenidoArchivoCifin,
            String exigenciaJudicialOverride) {
        if (validationBaseUrl.isBlank()) {
            throw new IllegalStateException(
                    "Configure integracion.validation.base-url (URL base del validation-service, sin barra final).");
        }
        URI url = URI.create(trimTrailingSlashes(validationBaseUrl) + "/validar");

        var payload = new ValidacionHttpRequest();
        payload.setDocumentoIdentidad(documentoIdentidad);
        payload.setNombreSolicitante(nombreVendedor);
        payload.setContenidoArchivoCifin(contenidoArchivoCifin);
        payload.setExigenciaJudicialDirector(normalizarJudicial(exigenciaJudicialOverride));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ValidacionHttpRequest> entity = new HttpEntity<>(payload, headers);

        log.info("Invocando validation-service POST {}", url);

        try {
            ResponseEntity<ValidacionHttpResponse> resp =
                    restTemplate.postForEntity(url, entity, ValidacionHttpResponse.class);
            if (resp.getBody() == null || resp.getBody().getEstadoVendedor() == null || resp.getBody().getEstadoVendedor().isBlank()) {
                throw new IllegalStateException("Respuesta de validación vacía o sin estadoVendedor.");
            }
            String rawEstado = resp.getBody().getEstadoVendedor().trim();
            log.info("validation-service respondió HTTP {} estadoVendedor={}", resp.getStatusCode().value(), rawEstado);
            try {
                SolicitudEstado estado = SolicitudEstado.valueOf(rawEstado);
                String detalle = resumirObservaciones(resp.getBody().getObservaciones());
                return new ValidacionVendedorResult(estado, detalle);
            } catch (IllegalArgumentException ex) {
                throw new IllegalStateException(
                        "Estado devuelto por validation-service no mapea a SolicitudEstado: '" + rawEstado + "'", ex);
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new IllegalStateException(
                    "validation-service respondió HTTP "
                            + ex.getStatusCode().value()
                            + ": "
                            + ex.getResponseBodyAsString(),
                    ex);
        } catch (ResourceAccessException ex) {
            throw new IllegalStateException(
                    "Timeout o error de red al contactar validation-service (" + url.getHost() + "): " + ex.getMessage(),
                    ex);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Fallo llamando al servicio de validación: " + ex.getMessage(), ex);
        }
    }

    private static String normalizarJudicial(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String u = raw.trim().toUpperCase();
        if ("REQUERIDO".equals(u) || "NO_REQUERIDO".equals(u)) {
            return u;
        }
        throw new IllegalArgumentException("exigenciaJudicial debe ser REQUERIDO, NO_REQUERIDO o vacío.");
    }

    private static String resumirObservaciones(List<String> obs) {
        if (obs == null || obs.isEmpty()) {
            return null;
        }
        String joined = obs.stream().filter(s -> s != null && !s.isBlank()).collect(Collectors.joining(" | "));
        if (joined.isEmpty()) {
            return null;
        }
        return joined.length() > 500 ? joined.substring(0, 497) + "…" : joined;
    }

    private static String trimTrailingSlashes(String url) {
        String s = url.trim();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValidacionHttpRequest {
        private String documentoIdentidad;
        private String nombreSolicitante;
        private String contenidoArchivoCifin;
        /** REQUERIDO | NO_REQUERIDO para serialización JSON hacia validation-service. */
        private String exigenciaJudicialDirector;

        public String getDocumentoIdentidad() {
            return documentoIdentidad;
        }

        public void setDocumentoIdentidad(String documentoIdentidad) {
            this.documentoIdentidad = documentoIdentidad;
        }

        public String getNombreSolicitante() {
            return nombreSolicitante;
        }

        public void setNombreSolicitante(String nombreSolicitante) {
            this.nombreSolicitante = nombreSolicitante;
        }

        public String getContenidoArchivoCifin() {
            return contenidoArchivoCifin;
        }

        public void setContenidoArchivoCifin(String contenidoArchivoCifin) {
            this.contenidoArchivoCifin = contenidoArchivoCifin;
        }

        public String getExigenciaJudicialDirector() {
            return exigenciaJudicialDirector;
        }

        public void setExigenciaJudicialDirector(String exigenciaJudicialDirector) {
            this.exigenciaJudicialDirector = exigenciaJudicialDirector;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValidacionHttpResponse {
        private String estadoVendedor;
        private List<String> observaciones;

        public String getEstadoVendedor() {
            return estadoVendedor;
        }

        public void setEstadoVendedor(String estadoVendedor) {
            this.estadoVendedor = estadoVendedor;
        }

        public List<String> getObservaciones() {
            return observaciones;
        }

        public void setObservaciones(List<String> observaciones) {
            this.observaciones = observaciones;
        }
    }
}
