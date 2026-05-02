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
 * Flujo HTTP contra mock Datacrédito + archivo CIFIN + judicial mock.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ValidacionEndToEndIntegrationTest {

    @Autowired
    TestRestTemplate testRestTemplate;

    @Test
    void validar_aprobada_cuando_alta_alta_y_judicial_no_requerido() {
        String body = """
                {
                  "documentoIdentidad": "CC123456789",
                  "nombreSolicitante": "Ana Gómez",
                  "contenidoArchivoCifin": "CC123456789|700|NORMAL"
                }
                """;

        ResponseEntity<Map> resp = postValidar(body);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().get("apto")).isEqualTo(true);
        assertThat(resp.getBody().get("estadoVendedor")).isEqualTo("APROBADA");
        assertThat(resp.getBody().get("clasificacionDatacredito")).isEqualTo("ALTA");
        assertThat(resp.getBody().get("clasificacionCifin")).isEqualTo("ALTA");
        assertThat(resp.getBody().get("exigenciaJudicial")).isEqualTo("NO_REQUERIDO");
    }

    @Test
    void validar_rechazada_si_datacredito_baja_lista_control() {
        String body = """
                {
                  "documentoIdentidad": "CC00999",
                  "nombreSolicitante": "Riesgo Alto",
                  "contenidoArchivoCifin": "CC00999|500|NORMAL"
                }
                """;

        ResponseEntity<Map> resp = postValidar(body);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().get("apto")).isEqualTo(false);
        assertThat(resp.getBody().get("estadoVendedor")).isEqualTo("RECHAZADA");
        assertThat(resp.getBody().get("clasificacionDatacredito")).isEqualTo("BAJA");
    }

    @Test
    void validar_rechazada_si_cifin_baja() {
        String body = """
                {
                  "documentoIdentidad": "CC111111111",
                  "nombreSolicitante": "Test",
                  "contenidoArchivoCifin": "CC111111111|900|MORA"
                }
                """;

        ResponseEntity<Map> resp = postValidar(body);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("estadoVendedor")).isEqualTo("RECHAZADA");
        assertThat(resp.getBody().get("clasificacionCifin")).isEqualTo("BAJA");
    }

    @Test
    void validar_devuelta_si_advertencia_datacredito() {
        // mock Datacrédito: documento terminado en 555 → score 620 → clasificación ADVERTENCIA
        String body555 = """
                {
                  "documentoIdentidad": "XX555",
                  "nombreSolicitante": "Advertencia DC",
                  "contenidoArchivoCifin": "XX555|600|NORMAL"
                }
                """;

        ResponseEntity<Map> resp = postValidar(body555);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("clasificacionDatacredito")).isEqualTo("ADVERTENCIA");
        assertThat(resp.getBody().get("clasificacionCifin")).isEqualTo("ADVERTENCIA");
        assertThat(resp.getBody().get("estadoVendedor")).isEqualTo("DEVUELTA");
        assertThat(resp.getBody().get("apto")).isEqualTo(false);
    }

    @Test
    void score_cifin_cero_con_datacredito_alta_rechazada() {
        String body = """
                {
                  "documentoIdentidad": "CC-SCORE-0",
                  "nombreSolicitante": "Riesgo CIFIN bajo",
                  "contenidoArchivoCifin": "CC-SCORE-0|0|NORMAL"
                }
                """;

        ResponseEntity<Map> resp = postValidar(body);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("estadoVendedor")).isEqualTo("RECHAZADA");
        assertThat(resp.getBody().get("clasificacionCifin")).isEqualTo("BAJA");
        assertThat(resp.getBody().get("apto")).isEqualTo(false);
    }

    @Test
    void score_cifin_600_con_datacredito_alta_devuelta() {
        String body = """
                {
                  "documentoIdentidad": "CC-SCORE-600",
                  "nombreSolicitante": "Tramo advertencia",
                  "contenidoArchivoCifin": "CC-SCORE-600|600|NORMAL"
                }
                """;

        ResponseEntity<Map> resp = postValidar(body);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("estadoVendedor")).isEqualTo("DEVUELTA");
        assertThat(resp.getBody().get("clasificacionCifin")).isEqualTo("ADVERTENCIA");
        assertThat(resp.getBody().get("apto")).isEqualTo(false);
    }

    @Test
    void score_cifin_700_con_datacredito_alta_aprobada() {
        String body = """
                {
                  "documentoIdentidad": "CC-SCORE-700",
                  "nombreSolicitante": "Tramo aprobación",
                  "contenidoArchivoCifin": "CC-SCORE-700|700|NORMAL"
                }
                """;

        ResponseEntity<Map> resp = postValidar(body);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("estadoVendedor")).isEqualTo("APROBADA");
        assertThat(resp.getBody().get("clasificacionCifin")).isEqualTo("ALTA");
        assertThat(resp.getBody().get("apto")).isEqualTo(true);
    }

    @Test
    void validar_devuelta_si_judicial_requerido_aunque_creditos_alta() {
        String body = """
                {
                  "documentoIdentidad": "CC-JUD-001",
                  "nombreSolicitante": "Con marca judicial",
                  "contenidoArchivoCifin": "CC-JUD-001|700|NORMAL"
                }
                """;

        ResponseEntity<Map> resp = postValidar(body);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("exigenciaJudicial")).isEqualTo("REQUERIDO");
        assertThat(resp.getBody().get("estadoVendedor")).isEqualTo("DEVUELTA");
        assertThat(resp.getBody().get("apto")).isEqualTo(false);
    }

    private ResponseEntity<Map> postValidar(String jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        return testRestTemplate.exchange("/validar", HttpMethod.POST, entity, Map.class);
    }
}
