package com.marketplace.solicitud.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Notifica a user-service que debe sumar rol VENDEDOR al usuario registrado cuyo documento o correo coincide con la solicitud ACTIVA.
 */
@Component
public class UserServicePromoverVendedorClient {

    private static final Logger log = LoggerFactory.getLogger(UserServicePromoverVendedorClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String internalSecret;

    public UserServicePromoverVendedorClient(
            @Qualifier("userServiceRestTemplate") RestTemplate restTemplate,
            @Value("${integracion.user-service.base-url:}") String baseUrl,
            @Value("${integracion.user-service.internal-secret:}") String internalSecret) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl != null ? baseUrl.trim().replaceAll("/$", "") : "";
        this.internalSecret = internalSecret != null ? internalSecret : "";
    }

    public void notificarActivacionSiHayUsuarioRegistrado(String documentoIdentidad, String correoElectronico) {
        if (baseUrl.isBlank() || internalSecret.isBlank()) {
            return;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Secret", internalSecret);
        var body = new PromoverVendedorPayload(documentoIdentidad, correoElectronico);
        try {
            ResponseEntity<PromoverVendedorRespuesta> re =
                    restTemplate.postForEntity(
                            baseUrl + "/interno/promover-vendedor",
                            new HttpEntity<>(body, headers),
                            PromoverVendedorRespuesta.class);
            PromoverVendedorRespuesta resp = re.getBody();
            if (resp != null && !resp.aplicado()) {
                log.warn(
                        "promover-vendedor aplicado=false codigo={} (¿documento/correo distintos al registro?)",
                        resp.codigoRazon());
            } else {
                log.info("promover-vendedor contestado OK por user-service");
            }
        } catch (RestClientException ex) {
            log.warn("promover-vendedor en user-service no aplicado: {}", ex.getMessage());
        }
    }

    private record PromoverVendedorPayload(String documentoIdentidad, String correoElectronico) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PromoverVendedorRespuesta(boolean aplicado, String nombreUsuario, String codigoRazon) {}
}
