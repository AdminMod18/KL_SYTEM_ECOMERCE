package com.marketplace.auth.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class SolicitudConsultaClient {

    private static final Logger log = LoggerFactory.getLogger(SolicitudConsultaClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public SolicitudConsultaClient(
            RestTemplate restTemplate, @Value("${integration.solicitud.base-url:}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl != null ? baseUrl.trim().replaceAll("/$", "") : "";
    }

    public Optional<SolicitudSnapshot> obtener(Long id) {
        if (baseUrl.isBlank()) {
            return Optional.empty();
        }
        try {
            SolicitudSnapshot s =
                    restTemplate.getForObject(baseUrl + "/solicitudes/" + id, SolicitudSnapshot.class);
            return Optional.ofNullable(s);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            log.warn("Consulta solicitud id={} HTTP {}", id, ex.getStatusCode().value());
            return Optional.empty();
        } catch (RestClientException ex) {
            log.warn("Consulta solicitud id={}: {}", id, ex.getMessage());
            return Optional.empty();
        }
    }

    public boolean estaConfigurado() {
        return !baseUrl.isBlank();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SolicitudSnapshot(Long id, String documentoIdentidad, String correoElectronico, String estado) {}
}
