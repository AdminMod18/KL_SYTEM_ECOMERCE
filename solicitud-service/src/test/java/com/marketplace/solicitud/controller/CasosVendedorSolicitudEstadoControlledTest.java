package com.marketplace.solicitud.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.solicitud.integration.ValidacionVendedorGateway;
import com.marketplace.solicitud.integration.ValidacionVendedorResult;
import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.notification.SolicitudEstadoNotificador;
import com.marketplace.solicitud.support.CuerpoJsonSolicitud;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Simula el resultado agregado del validation-service (equivalente a los tres casos de política)
 * y valida que la solicitud quede en el estado esperado tras {@code POST .../validacion-automatica}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CasosVendedorSolicitudEstadoControlledTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ValidacionVendedorGateway validacionVendedorGateway;

    @MockBean
    private SolicitudEstadoNotificador solicitudEstadoNotificador;

    @Test
    @DisplayName("Caso 1 agregado: resultado RECHAZADA → solicitud RECHAZADA")
    void caso1_estadoRechazada() throws Exception {
        when(validacionVendedorGateway.ejecutarValidacion(anyString(), anyString(), anyString(), any()))
                .thenReturn(new ValidacionVendedorResult(SolicitudEstado.RECHAZADA, null));

        SolicitudCreada c = crearSolicitud();
        ejecutarValidacion(c)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));

        verify(solicitudEstadoNotificador).notificarResolucion(
                eq(SolicitudEstado.RECHAZADA), eq(c.id()), eq("Vendedor QA"), any(), any());
    }

    @Test
    @DisplayName("Caso 2 agregado: resultado DEVUELTA → solicitud DEVUELTA")
    void caso2_estadoDevuelta() throws Exception {
        when(validacionVendedorGateway.ejecutarValidacion(anyString(), anyString(), anyString(), any()))
                .thenReturn(new ValidacionVendedorResult(SolicitudEstado.DEVUELTA, null));

        SolicitudCreada c = crearSolicitud();
        ejecutarValidacion(c)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DEVUELTA"));

        verify(solicitudEstadoNotificador)
                .notificarResolucion(eq(SolicitudEstado.DEVUELTA), eq(c.id()), eq("Vendedor QA"), any(), any());
    }

    @Test
    @DisplayName("Caso 3 agregado: resultado APROBADA → solicitud APROBADA")
    void caso3_estadoAprobada() throws Exception {
        when(validacionVendedorGateway.ejecutarValidacion(anyString(), anyString(), anyString(), any()))
                .thenReturn(new ValidacionVendedorResult(SolicitudEstado.APROBADA, null));

        SolicitudCreada c = crearSolicitud();
        ejecutarValidacion(c)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));

        verify(solicitudEstadoNotificador).notificarResolucion(
                eq(SolicitudEstado.APROBADA), eq(c.id()), eq("Vendedor QA"), any(), any());
    }

    private String docUnico() {
        return "CC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    private SolicitudCreada crearSolicitud() throws Exception {
        String doc = docUnico();
        MvcResult r = mockMvc.perform(post("/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CuerpoJsonSolicitud.crearNaturalNombreMostrar(
                                objectMapper, doc, "Vendedor QA", "Vendedor", "QA")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn();
        JsonNode root = objectMapper.readTree(r.getResponse().getContentAsString());
        long id = root.get("id").asLong();
        assertThat(id).isPositive();
        return new SolicitudCreada(id, doc);
    }

    private org.springframework.test.web.servlet.ResultActions ejecutarValidacion(SolicitudCreada c) throws Exception {
        String body = "{\"contenidoArchivoCifin\":\"" + c.documento() + "|400|NORMAL\"}";
        return mockMvc.perform(post("/solicitudes/" + c.id() + "/validacion-automatica")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private record SolicitudCreada(long id, String documento) {
    }
}
