package com.marketplace.order.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Propósito: validar POST /orden con cálculo decorado (subtotal + IVA + envío).
 * Patrón: prueba MVC de integración.
 * Responsabilidad: comprobar 201 y total esperado para un caso determinístico.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrdenControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void crearOrden_aplicaDecoradoresDePrecio() throws Exception {
        // Subtotal 2 * 10 = 20; con IVA 19% => 23.8000; + envío 5 => 28.8000
        String json = """
                {
                  "clienteId": "cli-001",
                  "lineas": [
                    {"sku": "SKU-A", "cantidad": 2, "precioUnitario": 10.00}
                  ]
                }
                """;

        mockMvc.perform(post("/orden").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CREADA"))
                .andExpect(jsonPath("$.total").value(28.8000));
    }
}
