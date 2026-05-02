package com.marketplace.product.integration;

import com.marketplace.product.repository.ProductoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integración HTTP real hacia solicitud-service (mock server): solo ACTIVA permite crear producto.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = "integracion.solicitud.base-url=http://127.0.0.1:19907")
class VendedorActivoProductoIntegrationTest {

    private static final String S1 = "http://127.0.0.1:19907/solicitudes/1";
    private static final String S2 = "http://127.0.0.1:19907/solicitudes/2";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate solicitudCatalogoRestTemplate;

    @Autowired
    private ProductoRepository productoRepository;

    private MockRestServiceServer mockSolicitud;

    @BeforeEach
    void setUp() {
        productoRepository.deleteAll();
        mockSolicitud = MockRestServiceServer.bindTo(solicitudCatalogoRestTemplate).build();
    }

    @AfterEach
    void tearDown() {
        mockSolicitud.verify();
    }

    @Test
    @DisplayName("Vendedor PENDIENTE → crear producto 409")
    void crearProducto_rechazaSiSolicitudNoActiva() throws Exception {
        mockSolicitud
                .expect(requestTo(S1))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withSuccess(
                                """
                                {"id":1,"nombreVendedor":"X","documentoIdentidad":"CC1","estado":"PENDIENTE","creadoEn":"2026-01-01T00:00:00Z"}
                                """,
                                MediaType.APPLICATION_JSON));

        String json =
                """
                {"vendedorSolicitudId":1,"nombre":"Item","precio":10,"descripcion":"d","categorias":["A"]}
                """;
        mockMvc.perform(post("/productos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VENDEDOR_NO_ACTIVO"));
    }

    @Test
    @DisplayName("Vendedor ACTIVA → crear producto 201 y aparece en catálogo")
    void crearProducto_okSiSolicitudActiva() throws Exception {
        mockSolicitud
                .expect(requestTo(S2))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withSuccess(
                                """
                                {"id":2,"nombreVendedor":"Y","documentoIdentidad":"CC2","estado":"ACTIVA","creadoEn":"2026-01-01T00:00:00Z"}
                                """,
                                MediaType.APPLICATION_JSON));

        String json =
                """
                {"vendedorSolicitudId":2,"nombre":"Teclado","precio":45.5,"descripcion":"Mecánico","categorias":["Perifericos"]}
                """;
        mockMvc.perform(post("/productos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vendedorSolicitudId").value(2))
                .andExpect(jsonPath("$.nombre").value("Teclado"));

        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].vendedorSolicitudId").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Teclado"));
    }
}
