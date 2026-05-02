package com.marketplace.auth.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Component
public class UserServiceRolesClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceRolesClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String internalSecret;

    public UserServiceRolesClient(
            RestTemplate restTemplate,
            @Value("${integration.user-service.base-url:}") String baseUrl,
            @Value("${integration.user-service.internal-secret:}") String internalSecret) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl != null ? baseUrl.trim().replaceAll("/$", "") : "";
        this.internalSecret = internalSecret != null ? internalSecret : "";
    }

    public Optional<List<String>> fetchRolesByNombreUsuario(String nombreUsuario) {
        if (baseUrl.isBlank() || internalSecret.isBlank()) {
            return Optional.empty();
        }
        String nu = nombreUsuario != null ? nombreUsuario.trim() : "";
        if (nu.isEmpty()) {
            return Optional.empty();
        }
        try {
            String encoded = UriUtils.encodePathSegment(nu, StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Secret", internalSecret);
            ResponseEntity<RolesListaDto> response =
                    restTemplate.exchange(
                            baseUrl + "/interno/usuarios/" + encoded + "/roles",
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            RolesListaDto.class);
            RolesListaDto body = response.getBody();
            if (body == null || body.roles() == null) {
                return Optional.empty();
            }
            return Optional.of(body.roles());
        } catch (HttpClientErrorException ex) {
            HttpStatusCode code = ex.getStatusCode();
            if (code.value() == 404) {
                return Optional.empty();
            }
            log.warn("Error HTTP consultando roles usuario={} status={}", nombreUsuario, code.value());
            return Optional.empty();
        } catch (RestClientException ex) {
            log.warn("No se pudieron obtener roles desde user-service para usuario={}: {}", nombreUsuario, ex.getMessage());
            return Optional.empty();
        }
    }

    private record RolesListaDto(List<String> roles) {}
}
