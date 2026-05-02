package com.marketplace.solicitud.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.notification.SolicitudEstadoNotificador;
import com.marketplace.solicitud.support.CuerpoJsonSolicitud;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Reglas de negocio: el resultado de {@link ValidacionVendedorGateway} determina si la solicitud puede activarse;
 * solo {@link SolicitudEstado#APROBADA} admite {@code POST /activacion-vendedor}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = "integracion.payment.base-url=http://127.0.0.1:19106")
class FlujoValidacionActivacionPorScoreIntegrationTest {

    private static final String PAYMENT_URL = "http://127.0.0.1:19106/pagos";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("paymentRestTemplate")
    private RestTemplate paymentRestTemplate;

    @MockBean
    private ValidacionVendedorGateway validacionVendedorGateway;

    @MockBean
    private SolicitudEstadoNotificador solicitudEstadoNotificador;

    private MockRestServiceServer mockPaymentServer;

    @BeforeEach
    void setUp() {
        mockPaymentServer = MockRestServiceServer.bindTo(paymentRestTemplate).build();
    }

    @AfterEach
    void tearDown() {
        mockPaymentServer.verify();
    }

    @Test
    @DisplayName("Caso score bajo (0): validación → RECHAZADA; activación → 409")
    void scoreCero_rechazada_noActiva() throws Exception {
        when(validacionVendedorGateway.ejecutarValidacion(anyString(), anyString(), contains("|0|"), any()))
                .thenReturn(new ValidacionVendedorResult(SolicitudEstado.RECHAZADA, null));

        IdDoc creada = crearSolicitudPendiente();

        mockMvc.perform(post("/solicitudes/" + creada.id + "/validacion-automatica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidacion(creada.documento, 0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));

        mockMvc.perform(post("/solicitudes/" + creada.id + "/activacion-vendedor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"tipo":"ONLINE","monto":10,"tokenPasarela":"tok"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Caso score medio (600): validación → DEVUELTA; activación → 409")
    void score600_devuelta_noActiva() throws Exception {
        when(validacionVendedorGateway.ejecutarValidacion(anyString(), anyString(), contains("|600|"), any()))
                .thenReturn(new ValidacionVendedorResult(SolicitudEstado.DEVUELTA, null));

        IdDoc creada = crearSolicitudPendiente();

        mockMvc.perform(post("/solicitudes/" + creada.id + "/validacion-automatica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidacion(creada.documento, 600)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DEVUELTA"));

        mockMvc.perform(post("/solicitudes/" + creada.id + "/activacion-vendedor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"tipo":"ONLINE","monto":10,"tokenPasarela":"tok"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Caso score alto (700): validación → APROBADA; activación con pago exitoso → ACTIVA")
    void score700_aprobada_activacionExitosa() throws Exception {
        when(validacionVendedorGateway.ejecutarValidacion(anyString(), anyString(), contains("|700|"), any()))
                .thenReturn(new ValidacionVendedorResult(SolicitudEstado.APROBADA, null));

        mockPaymentServer
                .expect(requestTo(PAYMENT_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "idTransaccion": "cccccccc-cccc-cccc-cccc-cccccccccccc",
                                  "estado": "AUTORIZADO",
                                  "mensaje": "Pasarela OK",
                                  "tipo": "ONLINE"
                                }
                                """,
                                MediaType.APPLICATION_JSON));

        IdDoc creada = crearSolicitudPendiente();

        mockMvc.perform(post("/solicitudes/" + creada.id + "/validacion-automatica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidacion(creada.documento, 700)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));

        mockMvc.perform(post("/solicitudes/" + creada.id + "/activacion-vendedor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"tipo":"ONLINE","monto":99.90,"tokenPasarela":"tok_demo_score"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    private IdDoc crearSolicitudPendiente() throws Exception {
        String doc = "CC" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        MvcResult c = mockMvc.perform(post("/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CuerpoJsonSolicitud.crearNaturalNombreMostrar(
                                objectMapper, doc, "Score QA", "Score", "QA")))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(c.getResponse().getContentAsString()).get("id").asLong();
        return new IdDoc(id, doc);
    }

    private String bodyValidacion(String documento, int score) {
        return "{\"documento\":\"" + documento + "\",\"score\":" + score + "}";
    }

    private record IdDoc(long id, String documento) {}
}
