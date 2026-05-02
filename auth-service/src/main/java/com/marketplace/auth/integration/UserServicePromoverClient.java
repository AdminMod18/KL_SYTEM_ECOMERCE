package com.marketplace.auth.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Component
public class UserServicePromoverClient {

    private static final Logger log = LoggerFactory.getLogger(UserServicePromoverClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String internalSecret;

    public UserServicePromoverClient(
            RestTemplate restTemplate,
            @Value("${integration.user-service.base-url:}") String baseUrl,
            @Value("${integration.user-service.internal-secret:}") String internalSecret) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl != null ? baseUrl.trim().replaceAll("/$", "") : "";
        this.internalSecret = internalSecret != null ? internalSecret : "";
    }

    public Optional<PromoverResult> promoverVendedor(String documentoIdentidad, String correoElectronico) {
        if (baseUrl.isBlank() || internalSecret.isBlank()) {
            return Optional.empty();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Secret", internalSecret);
        Map<String, String> body = new java.util.HashMap<>();
        if (documentoIdentidad != null && !documentoIdentidad.isBlank()) {
            body.put("documentoIdentidad", documentoIdentidad.trim());
        }
        if (correoElectronico != null && !correoElectronico.isBlank()) {
            body.put("correoElectronico", correoElectronico.trim());
        }
        if (body.isEmpty()) {
            return Optional.empty();
        }
        try {
            var response =
                    restTemplate.postForEntity(
                            baseUrl + "/interno/promover-vendedor",
                            new HttpEntity<>(body, headers),
                            PromoverResult.class);
            return Optional.ofNullable(response.getBody());
        } catch (RestClientException ex) {
            log.warn("promover-vendedor: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PromoverResult(boolean aplicado, String nombreUsuario, String codigoRazon) {}
}
