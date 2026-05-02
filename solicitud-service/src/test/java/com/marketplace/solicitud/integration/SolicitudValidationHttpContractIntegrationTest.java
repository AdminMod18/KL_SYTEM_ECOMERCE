package com.marketplace.solicitud.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.marketplace.solicitud.support.CuerpoJsonSolicitud;

/**
 * Verifica que solicitud-service construye bien la URL, llama a {@code POST /validar},
 * deserializa la respuesta y persiste el estado según {@code estadoVendedor}.
 * El validation-service real no se levanta: se usa {@link MockRestServiceServer} sobre el mismo {@link RestTemplate}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(
        properties = {
                "integracion.validation.base-url=http://127.0.0.1:19876",
                "integracion.validation.connect-timeout=2s",
                "integracion.validation.read-timeout=5s",
        })
class SolicitudValidationHttpContractIntegrationTest {

    private static final String VALIDATION_URL = "http://127.0.0.1:19876/validar";

    @Autowired
    @Qualifier("validationRestTemplate")
    private RestTemplate validationRestTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.bindTo(validationRestTemplate).build();
    }

    @AfterEach
    void tearDown() {
        mockServer.verify();
    }

    @Test
    @DisplayName("POST /validar correcto: cuerpo JSON y estado APROBADA persistido")
    void llamaValidacion_recibeRespuesta_aplicaEstado() throws Exception {
        String cuerpoValidacion =
                """
                {
                  "apto": true,
                  "estadoVendedor": "APROBADA",
                  "scoreDatacredito": 720,
                  "listaControlDatacredito": false,
                  "referenciaConsultaDatacredito": "REF-1",
                  "indicadorRiesgoCifin": 400,
                  "estadoLineaCifin": "NORMAL",
                  "lineaCifinEncontrada": true,
                  "clasificacionDatacredito": "ALTA",
                  "clasificacionCifin": "ALTA",
                  "exigenciaJudicial": "NO_REQUERIDO",
                  "observaciones": []
                }
                """;

        mockServer
                .expect(requestTo(VALIDATION_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Content-Type", org.hamcrest.Matchers.containsString("application/json")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("documentoIdentidad")))
                .andRespond(withSuccess(cuerpoValidacion, MediaType.APPLICATION_JSON));

        String doc = "CC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        MvcResult creada = mockMvc.perform(post("/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CuerpoJsonSolicitud.crearNaturalNombreMostrar(
                                objectMapper, doc, "Integración", "Test", "Integración")))
                .andExpect(status().isCreated())
                .andReturn();

        long id = objectMapper.readTree(creada.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/solicitudes/" + id + "/validacion-automatica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contenidoArchivoCifin\":\"" + doc + "|400|NORMAL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));
    }

    @Test
    @DisplayName("validation-service responde 400: solicitud-service propaga error claro")
    void errorHttp_clienteRecibe5xxOError() throws Exception {
        mockServer
                .expect(requestTo(VALIDATION_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest());

        String doc = "CC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        MvcResult creada = mockMvc.perform(post("/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CuerpoJsonSolicitud.crearNaturalNombreMostrar(objectMapper, doc, "Err", "Err", "Err")))
                .andExpect(status().isCreated())
                .andReturn();

        long id = objectMapper.readTree(creada.getResponse().getContentAsString()).get("id").asLong();

        MvcResult err = mockMvc.perform(post("/solicitudes/" + id + "/validacion-automatica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contenidoArchivoCifin\":\"" + doc + "|400|NORMAL\"}"))
                .andExpect(status().is5xxServerError())
                .andReturn();

        assertThat(err.getResponse().getContentAsString()).containsIgnoringCase("validation");
    }
}
