package com.marketplace.validation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Propósito: validar flujo HTTP real contra mock REST Datacrédito en el mismo proceso.
 * Patrón: prueba de integración extremo a extremo (puerto aleatorio).
 * Responsabilidad: asegurar que POST /validar invoca adaptadores y fachada coherentemente.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ValidacionEndToEndIntegrationTest {

    @Autowired
    TestRestTemplate testRestTemplate;

    @Test
    void validar_aprobado_cuando_datos_favorables() {
        String body = """
                {
                  "documentoIdentidad": "CC123456789",
                  "nombreSolicitante": "Ana Gómez",
                  "contenidoArchivoCifin": "CC123456789|600|NORMAL"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> resp = testRestTemplate.exchange(
                "/validar",
                HttpMethod.POST,
                entity,
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().get("apto")).isEqualTo(true);
    }

    @Test
    void validar_rechazado_si_documento_dispara_lista_control() {
        String body = """
                {
                  "documentoIdentidad": "CC00999",
                  "nombreSolicitante": "Riesgo Alto",
                  "contenidoArchivoCifin": "CC00999|600|NORMAL"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> resp = testRestTemplate.exchange(
                "/validar",
                HttpMethod.POST,
                entity,
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().get("apto")).isEqualTo(false);
    }
}
