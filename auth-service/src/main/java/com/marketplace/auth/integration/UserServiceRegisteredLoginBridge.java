package com.marketplace.auth.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Component
public class UserServiceRegisteredLoginBridge implements RegisteredUserLoginBridge {

    private static final Logger log = LoggerFactory.getLogger(UserServiceRegisteredLoginBridge.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String internalSecret;

    public UserServiceRegisteredLoginBridge(
            RestTemplate restTemplate,
            @Value("${integration.user-service.base-url:}") String baseUrl,
            @Value("${integration.user-service.internal-secret:}") String internalSecret) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl != null ? baseUrl.trim().replaceAll("/$", "") : "";
        this.internalSecret = internalSecret != null ? internalSecret : "";
    }

    @Override
    public Optional<Result> tryLogin(String usernameOrEmail, String rawPassword) {
        if (baseUrl.isBlank() || internalSecret.isBlank()) {
            return Optional.empty();
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Secret", internalSecret);
            Map<String, String> body =
                    Map.of("usernameOrEmail", usernameOrEmail, "password", rawPassword);
            ResponseEntity<UserServiceVerifyResponse> response =
                    restTemplate.exchange(
                            baseUrl + "/interno/verificar-credenciales",
                            HttpMethod.POST,
                            new HttpEntity<>(body, headers),
                            UserServiceVerifyResponse.class);
            UserServiceVerifyResponse dto = response.getBody();
            if (dto == null || dto.nombreUsuario() == null || dto.nombreUsuario().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(new Result(dto.nombreUsuario(), dto.roles() != null ? dto.roles() : java.util.List.of()));
        } catch (HttpClientErrorException ex) {
            return Optional.empty();
        } catch (RestClientException ex) {
            log.warn("No se pudo validar credenciales en user-service: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
