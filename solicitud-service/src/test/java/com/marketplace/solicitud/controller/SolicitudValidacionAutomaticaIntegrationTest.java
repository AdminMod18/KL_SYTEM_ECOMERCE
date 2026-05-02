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
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SolicitudValidacionAutomaticaIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ValidacionVendedorGateway validacionVendedorGateway;

    @MockBean
    SolicitudEstadoNotificador solicitudEstadoNotificador;

    @Test
    void crear_validarAutomatica_aplicaEstadoDelGateway() throws Exception {
        when(validacionVendedorGateway.ejecutarValidacion(anyString(), anyString(), anyString(), any()))
                .thenReturn(new ValidacionVendedorResult(SolicitudEstado.APROBADA, null));

        String doc = "CC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String bodyCrear =
                CuerpoJsonSolicitud.crearNaturalNombreMostrar(objectMapper, doc, "Tienda QA", "Tienda", "QA");

        MvcResult creada = mockMvc.perform(post("/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyCrear))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn();

        long id = leerId(creada);

        String cifin = "{\"contenidoArchivoCifin\":\"" + doc + "|500|NORMAL\"}";

        mockMvc.perform(post("/solicitudes/" + id + "/validacion-automatica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cifin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));

        verify(solicitudEstadoNotificador)
                .notificarResolucion(eq(SolicitudEstado.APROBADA), eq(id), eq("Tienda QA"), any(), any());
    }

    @Test
    void crear_validarAutomatica_conDocumentoYScore_sinContenidoArchivoCifin_ok() throws Exception {
        when(validacionVendedorGateway.ejecutarValidacion(anyString(), anyString(), anyString(), any()))
                .thenReturn(new ValidacionVendedorResult(SolicitudEstado.APROBADA, null));

        String doc = "CC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String bodyCrear = CuerpoJsonSolicitud.crearNaturalNombreMostrar(
                objectMapper, doc, "Tienda DocScore", "Tienda", "DocScore");

        MvcResult creada = mockMvc.perform(post("/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyCrear))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn();

        long id = leerId(creada);

        String bodyValidacion = "{\"documento\":\"" + doc + "\",\"score\":500}";

        mockMvc.perform(post("/solicitudes/" + id + "/validacion-automatica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));

        verify(validacionVendedorGateway)
                .ejecutarValidacion(eq(doc), eq("Tienda DocScore"), eq(doc + "|500|NORMAL"), isNull());
    }

    @Test
    void segunda_validacion_automatica_falla_si_ya_no_pendiente() throws Exception {
        when(validacionVendedorGateway.ejecutarValidacion(anyString(), anyString(), anyString(), any()))
                .thenReturn(new ValidacionVendedorResult(SolicitudEstado.APROBADA, null));

        String doc = "CC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String bodyCrear = CuerpoJsonSolicitud.crearNaturalNombreMostrar(objectMapper, doc, "Otra", "Otro", "Nombre");

        MvcResult creada = mockMvc.perform(post("/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyCrear))
                .andExpect(status().isCreated())
                .andReturn();

        long id = leerId(creada);

        String cifin = "{\"contenidoArchivoCifin\":\"" + doc + "|500|NORMAL\"}";

        mockMvc.perform(post("/solicitudes/" + id + "/validacion-automatica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cifin))
                .andExpect(status().isOk());

        mockMvc.perform(post("/solicitudes/" + id + "/validacion-automatica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cifin))
                .andExpect(status().isConflict());

        verify(solicitudEstadoNotificador, times(1))
                .notificarResolucion(eq(SolicitudEstado.APROBADA), eq(id), eq("Otra"), any(), any());
    }

    @Test
    void desde_devuelta_segunda_validacion_puede_aprobar() throws Exception {
        when(validacionVendedorGateway.ejecutarValidacion(anyString(), anyString(), anyString(), any()))
                .thenReturn(new ValidacionVendedorResult(SolicitudEstado.DEVUELTA, "obs"))
                .thenReturn(new ValidacionVendedorResult(SolicitudEstado.APROBADA, null));

        String doc = "CC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String bodyCrear =
                CuerpoJsonSolicitud.crearNaturalNombreMostrar(objectMapper, doc, "Reintento QA", "Reintento", "QA");

        MvcResult creada = mockMvc.perform(post("/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyCrear))
                .andExpect(status().isCreated())
                .andReturn();

        long id = leerId(creada);
        String bodyValidacion = "{\"documento\":\"" + doc + "\",\"score\":600}";

        mockMvc.perform(post("/solicitudes/" + id + "/validacion-automatica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DEVUELTA"));

        mockMvc.perform(post("/solicitudes/" + id + "/validacion-automatica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documento\":\"" + doc + "\",\"score\":700}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));

        verify(validacionVendedorGateway, times(2)).ejecutarValidacion(anyString(), anyString(), anyString(), any());
        verify(solicitudEstadoNotificador)
                .notificarResolucion(eq(SolicitudEstado.DEVUELTA), eq(id), eq("Reintento QA"), any(), any());
        verify(solicitudEstadoNotificador)
                .notificarResolucion(eq(SolicitudEstado.APROBADA), eq(id), eq("Reintento QA"), any(), any());
    }

    private long leerId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.get("id").asLong();
    }
}
