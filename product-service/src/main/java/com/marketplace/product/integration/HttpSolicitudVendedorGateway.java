package com.marketplace.product.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.marketplace.product.exception.ProductoBusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

/**
 * Cliente HTTP hacia {@code GET {base-url}/solicitudes/{id}} de solicitud-service.
 */
@Component
public class HttpSolicitudVendedorGateway implements VendedorActivoPort {

    private static final Logger log = LoggerFactory.getLogger(HttpSolicitudVendedorGateway.class);

    private static final String ESTADO_ACTIVA = "ACTIVA";

    private final RestTemplate restTemplate;
    private final String solicitudBaseUrl;

    public HttpSolicitudVendedorGateway(
            RestTemplate solicitudCatalogoRestTemplate,
            @Value("${integracion.solicitud.base-url:}") String solicitudBaseUrl) {
        this.restTemplate = solicitudCatalogoRestTemplate;
        this.solicitudBaseUrl = solicitudBaseUrl == null ? "" : solicitudBaseUrl.trim();
    }

    @Override
    public void assertVendedorEnEstadoActiva(long solicitudId) {
        if (solicitudBaseUrl.isBlank()) {
            throw new IllegalStateException(
                    "Configure integracion.solicitud.base-url (URL base del solicitud-service, sin barra final).");
        }
        URI url = URI.create(trimTrailingSlashes(solicitudBaseUrl) + "/solicitudes/" + solicitudId);
        log.info("Consultando estado de vendedor (solicitud) GET {}", url);

        try {
            ResponseEntity<SolicitudEstadoBody> resp = restTemplate.getForEntity(url, SolicitudEstadoBody.class);
            if (resp.getBody() == null || resp.getBody().estado() == null || resp.getBody().estado().isBlank()) {
                throw new IllegalStateException("Respuesta de solicitud-service sin estado.");
            }
            String estado = resp.getBody().estado().trim();
            log.info("solicitud-service respondió id={} estado={}", solicitudId, estado);
            if (!ESTADO_ACTIVA.equals(estado)) {
                throw new ProductoBusinessException(
                        "VENDEDOR_NO_ACTIVO",
                        "Solo vendedores con solicitud en estado ACTIVA pueden crear productos. Estado actual: "
                                + estado);
            }
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Solicitud de vendedor no encontrada: " + solicitudId);
            }
            throw new IllegalStateException(
                    "solicitud-service respondió HTTP "
                            + ex.getStatusCode().value()
                            + ": "
                            + ex.getResponseBodyAsString(),
                    ex);
        } catch (HttpServerErrorException ex) {
            throw new IllegalStateException(
                    "solicitud-service respondió HTTP "
                            + ex.getStatusCode().value()
                            + ": "
                            + ex.getResponseBodyAsString(),
                    ex);
        } catch (ResourceAccessException ex) {
            throw new IllegalStateException(
                    "Timeout o error de red al contactar solicitud-service (" + url.getHost() + "): " + ex.getMessage(),
                    ex);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Fallo consultando solicitud-service: " + ex.getMessage(), ex);
        }
    }

    private static String trimTrailingSlashes(String url) {
        String s = url.trim();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SolicitudEstadoBody(Long id, String estado) {}
}
