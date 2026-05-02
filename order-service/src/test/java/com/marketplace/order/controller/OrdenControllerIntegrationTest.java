package com.marketplace.order.controller;

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
 * Propósito: validar POST /orden con cálculo decorado (subtotal + IVA + comisión + envío).
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
        // Subtotal 20; IVA 19% sobre base => 3.80; con IVA multiplicativo cadena => 23.80; comisión 5% sobre base => 1.00; envío 5 => 29.8000
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
                .andExpect(jsonPath("$.tipoEntrega").value("DOMICILIO"))
                .andExpect(jsonPath("$.subtotalBase").value(20.0000))
                .andExpect(jsonPath("$.montoIva").value(3.8000))
                .andExpect(jsonPath("$.montoComision").value(1.0000))
                .andExpect(jsonPath("$.montoEnvio").value(5.0000))
                .andExpect(jsonPath("$.total").value(29.8000));
    }

    @Test
    void crearOrden_envioNacionalColombia_aplicaTarifaMayor() throws Exception {
        // Misma base que el caso anterior; Medellín → envío nacional 8 → total 24.8 + 8 = 32.8000
        String json = """
                {
                  "clienteId": "cli-002",
                  "paisEnvio": "Colombia",
                  "ciudadEnvio": "Medellín",
                  "lineas": [
                    {"sku": "SKU-A", "cantidad": 2, "precioUnitario": 10.00}
                  ]
                }
                """;

        mockMvc.perform(post("/orden").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.montoEnvio").value(8.0000))
                .andExpect(jsonPath("$.total").value(32.8000));
    }

    @Test
    void listarOrdenes_porCliente_ordenYordenes() throws Exception {
        String json = """
                {
                  "clienteId": "cli-historial",
                  "lineas": [
                    {"sku": "SKU-H", "cantidad": 1, "precioUnitario": 5.00}
                  ]
                }
                """;
        mockMvc.perform(post("/orden").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/ordenes").param("clienteId", "cli-historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].clienteId").value("cli-historial"));

        mockMvc.perform(get("/orden").param("clienteId", "cli-historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
