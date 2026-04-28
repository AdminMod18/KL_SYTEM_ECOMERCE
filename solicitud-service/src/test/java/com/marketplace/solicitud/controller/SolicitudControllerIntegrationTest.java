package com.marketplace.solicitud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.solicitud.model.SolicitudEstado;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Propósito: validar flujo HTTP principal de solicitudes en memoria (H2).
 * Patrón: Test de integración MVC con MockMvc.
 * Responsabilidad: comprobar POST/GET/PUT con contratos JSON esperados.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SolicitudControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void crearListarYCambiarEstado() throws Exception {
        String body = """
                {"nombreVendedor":"Tienda Demo","documentoIdentidad":"CC123456789"}
                """;

        mockMvc.perform(post("/solicitudes").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        mockMvc.perform(get("/solicitudes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(put("/solicitudes/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new EstadoDto(SolicitudEstado.APROBADA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));
    }

    record EstadoDto(SolicitudEstado estado) {
    }
}
