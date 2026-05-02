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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Flujo negocio: solicitud APROBADA → payment-service (HTTP) → ACTIVA.
 * Solo APROBADA puede usar {@code POST /solicitudes/{id}/activacion-vendedor}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = "integracion.payment.base-url=http://127.0.0.1:19105")
class FlujoPagoActivacionVendedorIntegrationTest {

    private static final String PAYMENT_URL = "http://127.0.0.1:19105/pagos";

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
        when(validacionVendedorGateway.ejecutarValidacion(anyString(), anyString(), anyString(), any()))
                .thenReturn(new ValidacionVendedorResult(SolicitudEstado.APROBADA, null));
    }

    @AfterEach
    void tearDown() {
        mockPaymentServer.verify();
    }

    @Test
    @DisplayName("APROBADA + pago ONLINE AUTORIZADO → solicitud ACTIVA")
    void activacionTrasPago_ok() throws Exception {
        mockPaymentServer
                .expect(requestTo(PAYMENT_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "idTransaccion": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
                                  "estado": "AUTORIZADO",
                                  "mensaje": "Pasarela OK",
                                  "tipo": "ONLINE"
                                }
                                """,
                                MediaType.APPLICATION_JSON));

        long id = crearSolicitudYaprobar();

        String activacion =
                """
                {"tipo":"ONLINE","monto":99.90,"tokenPasarela":"tok_demo_qa"}
                """;
        mockMvc.perform(post("/solicitudes/" + id + "/activacion-vendedor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/solicitudes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    @DisplayName("PENDIENTE no puede activarse con pago (409)")
    void activacionTrasPago_rechazaSiNoAprobada() throws Exception {
        String doc = "CC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        MvcResult c = mockMvc.perform(post("/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CuerpoJsonSolicitud.crearNaturalNombreMostrar(
                                objectMapper, doc, "Sin aprobar", "Sin", "Aprobar")))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(c.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/solicitudes/" + id + "/activacion-vendedor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"tipo":"ONLINE","monto":10,"tokenPasarela":"tok"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Pago con estado no exitoso → 402")
    void activacionTrasPago_fallaSiEstadoPagoNoEsExitoso() throws Exception {
        mockPaymentServer
                .expect(requestTo(PAYMENT_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "idTransaccion": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                                  "estado": "DECLINADO",
                                  "mensaje": "simulación rechazo",
                                  "tipo": "ONLINE"
                                }
                                """,
                                MediaType.APPLICATION_JSON));

        long id = crearSolicitudYaprobar();

        mockMvc.perform(post("/solicitudes/" + id + "/activacion-vendedor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"tipo":"ONLINE","monto":10,"tokenPasarela":"tok"}
                                """))
                .andExpect(status().is(402));

        mockMvc.perform(get("/solicitudes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));
    }

    @Test
    @DisplayName("payment-service HTTP 400 → 502 y solicitud sigue APROBADA")
    void activacionTrasPago_propagaErrorPayment() throws Exception {
        mockPaymentServer.expect(requestTo(PAYMENT_URL)).andExpect(method(HttpMethod.POST)).andRespond(withBadRequest());

        long id = crearSolicitudYaprobar();

        mockMvc.perform(post("/solicitudes/" + id + "/activacion-vendedor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"tipo":"ONLINE","monto":10,"tokenPasarela":"tok"}
                                """))
                .andExpect(status().isBadGateway());

        mockMvc.perform(get("/solicitudes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));
    }

    private long crearSolicitudYaprobar() throws Exception {
        String doc = "CC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        MvcResult c = mockMvc.perform(post("/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CuerpoJsonSolicitud.crearNaturalNombreMostrar(
                                objectMapper, doc, "Pago QA", "Pago", "QA")))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(c.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/solicitudes/" + id + "/validacion-automatica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documento\":\"" + doc + "\",\"score\":700}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));
        return id;
    }
}
