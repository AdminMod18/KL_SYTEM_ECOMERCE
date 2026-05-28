package com.marketplace.product.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marketplace.product.integration.VendedorActivoPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Propósito: validar endpoints de publicación y listado de productos.
 * Patrón: integración MVC.
 * Responsabilidad: comprobar POST/GET y ruta de categoría creada con Composite.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductoControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private VendedorActivoPort vendedorActivoPort;

    @BeforeEach
    void vendedorSiempreAutorizadoEnEsteTest() {
        doNothing().when(vendedorActivoPort).assertVendedorEnEstadoActiva(anyLong());
    }

    @Test
    @Order(1)
    void crearYListarProducto() throws Exception {
        String json = """
                {
                  "vendedorSolicitudId": 100,
                  "nombre": "Portátil Pro",
                  "precio": 3500.50,
                  "descripcion": "Equipo para desarrollo",
                  "categorias": ["Tecnologia", "Computadores", "Portatiles"]
                }
                """;

        mockMvc.perform(post("/productos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vendedorSolicitudId").value(100))
                .andExpect(jsonPath("$.rutaCategoria").value("CATALOGO/Tecnologia/Computadores/Portatiles"));

        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @Order(2)
    void listarInteracciones_productoExiste_devuelve200yLista() throws Exception {
        String json = """
                {
                  "vendedorSolicitudId": 200,
                  "nombre": "Mouse X",
                  "precio": 29.99,
                  "descripcion": "Demo",
                  "categorias": ["Tecnologia", "Perifericos"]
                }
                """;
        String body =
                mockMvc.perform(post("/productos").contentType(MediaType.APPLICATION_JSON).content(json))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        JsonNode root = objectMapper.readTree(body);
        long productoId = root.get("id").asLong();

        mockMvc.perform(get("/productos/" + productoId + "/interacciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Order(3)
    void crearProducto_conVariasImagenesUrlLargas_aceptado() throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("vendedorSolicitudId", 300);
        body.put("nombre", "Producto varias fotos");
        body.put("precio", 49.99);
        body.put("descripcion", "Demo varias tomas / data URL");
        body.set("categorias", objectMapper.createArrayNode().add("Tecnologia"));
        ArrayNode imgs = objectMapper.createArrayNode();
        imgs.add("https://cdn.test/img.jpg?" + "p".repeat(900));
        imgs.add("data:image/jpeg;base64," + "Z".repeat(2500));
        body.set("imagenesUrls", imgs);

        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imagenesUrls").exists());
    }

    @Test
    @Order(4)
    void listarPorVendedor_yActualizarStock() throws Exception {
        String json = """
                {
                  "vendedorSolicitudId": 400,
                  "nombre": "Stock Demo",
                  "precio": 99.99,
                  "descripcion": "Inventario",
                  "categorias": ["Tecnologia", "Accesorios"],
                  "cantidadStock": 3
                }
                """;
        String body =
                mockMvc.perform(post("/productos").contentType(MediaType.APPLICATION_JSON).content(json))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        long productoId = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(get("/productos").param("vendedorSolicitudId", "400"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cantidadStock").value(3));

        String patch = """
                {"vendedorSolicitudId":400,"cantidadStock":15}
                """;
        mockMvc.perform(patch("/productos/" + productoId + "/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadStock").value(15));
    }
}
