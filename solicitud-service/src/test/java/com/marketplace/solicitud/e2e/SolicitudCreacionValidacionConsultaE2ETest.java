package com.marketplace.solicitud.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.notification.SolicitudEstadoNotificador;
import com.marketplace.solicitud.support.CuerpoJsonSolicitud;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * E2E: crear solicitud → validación automática (HTTP mock a validation-service) → consultar listado y comprobar estado.
 * Regla de negocio: desde {@link SolicitudEstado#PENDIENTE} solo pueden resultar APROBADA, RECHAZADA o DEVUELTA
 * tras la validación automática (coincidencia con {@code estadoVendedor} del servicio externo).
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
class SolicitudCreacionValidacionConsultaE2ETest {

    private static final String VALIDATION_URL = "http://127.0.0.1:19876/validar";

    @Autowired
    @Qualifier("validationRestTemplate")
    private RestTemplate validationRestTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SolicitudEstadoNotificador solicitudEstadoNotificador;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.bindTo(validationRestTemplate).build();
    }

    @AfterEach
    void tearDown() {
        mockServer.verify();
    }

    @ParameterizedTest
    @EnumSource(value = SolicitudEstado.class, names = {"APROBADA", "RECHAZADA", "DEVUELTA"})
    @DisplayName("E2E: crear → validar → listar; estado persistido = estadoVendedor del validation-service")
    void flujoCompleto_estadoCoincideConPoliticaPendiente(SolicitudEstado estadoVendedor) throws Exception {
        assertThat(estadoVendedor)
                .as("Regla: validación automática solo cierra en APROBADA, RECHAZADA o DEVUELTA")
                .isIn(SolicitudEstado.APROBADA, SolicitudEstado.RECHAZADA, SolicitudEstado.DEVUELTA);

        String cuerpoValidacion = cuerpoJsonValidacion(estadoVendedor);
        mockServer
                .expect(requestTo(VALIDATION_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Content-Type", org.hamcrest.Matchers.containsString("application/json")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("documentoIdentidad")))
                .andRespond(withSuccess(cuerpoValidacion, MediaType.APPLICATION_JSON));

        String doc = "CC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String bodyCrear =
                CuerpoJsonSolicitud.crearNaturalNombreMostrar(
                        objectMapper, doc, "Vendedor E2E", "Vendedor", "E2E");
        String contenidoCifin = doc + "|400|NORMAL";
        String bodyValidacion = "{\"contenidoArchivoCifin\":\"" + contenidoCifin + "\"}";

        System.out.println();
        System.out.println("=== E2E solicitud (caso " + estadoVendedor + ") ===");
        System.out.println("[1] Datos entrada — crear solicitud: " + bodyCrear.replaceAll("\\s+", " ").trim());
        System.out.println("[1] Datos entrada — validación automática: " + bodyValidacion);
        System.out.println(
                "[2] Resultado validación — estadoVendedor simulado (validation-service): " + estadoVendedor.name());

        MvcResult creada = mockMvc.perform(post("/solicitudes").contentType(MediaType.APPLICATION_JSON).content(bodyCrear))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn();

        long id = objectMapper.readTree(creada.getResponse().getContentAsString()).get("id").asLong();

        MvcResult trasValidar = mockMvc.perform(post("/solicitudes/" + id + "/validacion-automatica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(estadoVendedor.name()))
                .andReturn();

        String cuerpoRespuestaValidacion = trasValidar.getResponse().getContentAsString();
        JsonNode nodoValidacion = objectMapper.readTree(cuerpoRespuestaValidacion);
        String estadoTrasPost = nodoValidacion.get("estado").asText();

        System.out.println(
                "[2] Resultado validación — estado en respuesta POST /solicitudes/{id}/validacion-automatica: "
                        + estadoTrasPost);

        MvcResult porId = mockMvc.perform(get("/solicitudes/" + id)).andExpect(status().isOk()).andReturn();
        String estadoFinalListado =
                objectMapper.readTree(porId.getResponse().getContentAsString()).get("estado").asText();

        System.out.println("[3] Estado final — GET /solicitudes/{id} (id=" + id + "): " + estadoFinalListado);
        System.out.println("=== Fin E2E ===");
        System.out.println();

        assertThat(estadoTrasPost).isEqualTo(estadoVendedor.name());
        assertThat(estadoFinalListado).isEqualTo(estadoVendedor.name());
        assertThat(estadoTrasPost).isEqualTo(estadoFinalListado);
    }

    private static String cuerpoJsonValidacion(SolicitudEstado estadoVendedor) {
        return """
                {
                  "apto": true,
                  "estadoVendedor": "%s",
                  "scoreDatacredito": 720,
                  "listaControlDatacredito": false,
                  "referenciaConsultaDatacredito": "REF-E2E",
                  "indicadorRiesgoCifin": 400,
                  "estadoLineaCifin": "NORMAL",
                  "lineaCifinEncontrada": true,
                  "clasificacionDatacredito": "ALTA",
                  "clasificacionCifin": "ALTA",
                  "exigenciaJudicial": "NO_REQUERIDO",
                  "observaciones": []
                }
                """
                .formatted(estadoVendedor.name());
    }
}
