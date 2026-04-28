package com.marketplace.analytics.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Propósito: validar ingesta de eventos y cálculo de KPIs vía REST.
 * Patrón: prueba MVC de integración.
 * Responsabilidad: comprobar POST /eventos y GET /kpis con agregados esperados.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AnalyticsControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void registrarEventosYConsultarKpis() throws Exception {
        mockMvc.perform(post("/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"COMPRA\",\"referencia\":\"ORD-1\",\"valorMonetario\":100.50}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"COMPRA\",\"referencia\":\"ORD-2\",\"valorMonetario\":49.50}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"SOLICITUD_APROBADA\",\"referencia\":\"SOL-9\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/kpis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEventos").value(3))
                .andExpect(jsonPath("$.comprasRegistradas").value(2))
                .andExpect(jsonPath("$.ingresosComprasAcumulados").value(150.0000))
                .andExpect(jsonPath("$.solicitudesAprobadasRegistradas").value(1));
    }
}
