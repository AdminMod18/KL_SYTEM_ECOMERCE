package com.marketplace.product.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class ProductoControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void crearYListarProducto() throws Exception {
        String json = """
                {
                  "nombre": "Portátil Pro",
                  "precio": 3500.50,
                  "descripcion": "Equipo para desarrollo",
                  "categorias": ["Tecnologia", "Computadores", "Portatiles"]
                }
                """;

        mockMvc.perform(post("/productos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rutaCategoria").value("CATALOGO/Tecnologia/Computadores/Portatiles"));

        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
