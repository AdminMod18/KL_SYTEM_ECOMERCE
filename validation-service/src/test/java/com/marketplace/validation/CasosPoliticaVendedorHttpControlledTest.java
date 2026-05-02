package com.marketplace.validation;

import com.marketplace.validation.domain.ExigenciaJudicial;
import com.marketplace.validation.domain.ResultadoCifin;
import com.marketplace.validation.domain.ResultadoDatacredito;
import com.marketplace.validation.port.CifinArchivoPort;
import com.marketplace.validation.port.DatacreditoConsultaPort;
import com.marketplace.validation.port.JudicialConsultaPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Misma política que {@link com.marketplace.validation.facade.CasosPoliticaVendedorControlledTest},
 * pero flujo completo HTTP {@code POST /validar} con puertos simulados.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CasosPoliticaVendedorHttpControlledTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private DatacreditoConsultaPort datacreditoConsultaPort;

    @MockBean
    private CifinArchivoPort cifinArchivoPort;

    @MockBean
    private JudicialConsultaPort judicialConsultaPort;

    @Test
    @DisplayName("HTTP Caso 1: DC BAJA + CIFIN ALTA → RECHAZADA")
    void http_caso1() {
        when(datacreditoConsultaPort.consultarPorDocumento(anyString()))
                .thenReturn(new ResultadoDatacredito(300, false, "SIM-HTTP-1"));
        when(cifinArchivoPort.interpretarParaDocumento(anyString(), anyString()))
                .thenReturn(new ResultadoCifin(700, "NORMAL", true));
        when(judicialConsultaPort.consultarExigenciaPorDocumento(anyString()))
                .thenReturn(ExigenciaJudicial.NO_REQUERIDO);

        ResponseEntity<Map> resp = postValidar("CC-HTTP-1");
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("estadoVendedor")).isEqualTo("RECHAZADA");
        assertThat(resp.getBody().get("apto")).isEqualTo(false);
    }

    @Test
    @DisplayName("HTTP Caso 2: DC ADVERTENCIA + CIFIN ALTA → DEVUELTA")
    void http_caso2() {
        when(datacreditoConsultaPort.consultarPorDocumento(anyString()))
                .thenReturn(new ResultadoDatacredito(620, false, "SIM-HTTP-2"));
        when(cifinArchivoPort.interpretarParaDocumento(anyString(), anyString()))
                .thenReturn(new ResultadoCifin(700, "NORMAL", true));
        when(judicialConsultaPort.consultarExigenciaPorDocumento(anyString()))
                .thenReturn(ExigenciaJudicial.NO_REQUERIDO);

        ResponseEntity<Map> resp = postValidar("CC-HTTP-2");
        assertThat(resp.getBody().get("estadoVendedor")).isEqualTo("DEVUELTA");
        assertThat(resp.getBody().get("apto")).isEqualTo(false);
    }

    @Test
    @DisplayName("HTTP Caso 3: DC ALTA + CIFIN ALTA + judicial NO_REQUERIDO → APROBADA")
    void http_caso3() {
        when(datacreditoConsultaPort.consultarPorDocumento(anyString()))
                .thenReturn(new ResultadoDatacredito(720, false, "SIM-HTTP-3"));
        when(cifinArchivoPort.interpretarParaDocumento(anyString(), anyString()))
                .thenReturn(new ResultadoCifin(700, "NORMAL", true));
        when(judicialConsultaPort.consultarExigenciaPorDocumento(anyString()))
                .thenReturn(ExigenciaJudicial.NO_REQUERIDO);

        ResponseEntity<Map> resp = postValidar("CC-HTTP-3");
        assertThat(resp.getBody().get("estadoVendedor")).isEqualTo("APROBADA");
        assertThat(resp.getBody().get("apto")).isEqualTo(true);
    }

    private ResponseEntity<Map> postValidar(String documento) {
        String body = """
                {
                  "documentoIdentidad": "%s",
                  "nombreSolicitante": "Simulado",
                  "contenidoArchivoCifin": "%s|700|NORMAL"
                }
                """.formatted(documento, documento);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return testRestTemplate.exchange("/validar", HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
    }
}
