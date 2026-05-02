package com.marketplace.solicitud.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.solicitud.integration.ValidacionVendedorGateway;
import com.marketplace.solicitud.integration.ValidacionVendedorResult;
import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.notification.SolicitudEstadoNotificador;
import com.marketplace.solicitud.support.CuerpoJsonSolicitud;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    @MockBean
    SolicitudEstadoNotificador solicitudEstadoNotificador;

    @MockBean
    ValidacionVendedorGateway validacionVendedorGateway;

    @Test
    void crearListarYCambiarEstado() throws Exception {
        when(validacionVendedorGateway.ejecutarValidacion(anyString(), anyString(), anyString(), any()))
                .thenReturn(new ValidacionVendedorResult(SolicitudEstado.APROBADA, null));

        String doc = "CC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String body = CuerpoJsonSolicitud.crearNatural(objectMapper, doc, "Tienda", "Demo");

        MvcResult creada = mockMvc.perform(post("/solicitudes").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn();

        JsonNode root = objectMapper.readTree(creada.getResponse().getContentAsString());
        long id = root.get("id").asLong();

        mockMvc.perform(get("/solicitudes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.documentoIdentidad").value(doc))
                .andExpect(jsonPath("$.nombreVendedor").value("Tienda Demo"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        mockMvc.perform(get("/solicitudes/999999999")).andExpect(status().isNotFound());

        mockMvc.perform(put("/solicitudes/" + id + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EstadoDto(SolicitudEstado.ACTIVA))))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/solicitudes/" + id + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EstadoDto(SolicitudEstado.APROBADA))))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/solicitudes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));

        String bodyValidacion = "{\"documento\":\"" + doc + "\",\"score\":700}";
        mockMvc.perform(post("/solicitudes/" + id + "/validacion-automatica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));

        mockMvc.perform(get("/solicitudes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));

        verify(solicitudEstadoNotificador).notificarResolucion(
                eq(SolicitudEstado.APROBADA), eq(id), eq("Tienda Demo"), any(), any());
    }

    @Test
    void reputacionResumen_solicitudInexistente_devuelve200conCeros() throws Exception {
        mockMvc.perform(get("/solicitudes/999999998/reputacion-resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solicitudId").value(999999998))
                .andExpect(jsonPath("$.totalCalificaciones").value(0));
    }

    record EstadoDto(SolicitudEstado estado) {
    }
}
