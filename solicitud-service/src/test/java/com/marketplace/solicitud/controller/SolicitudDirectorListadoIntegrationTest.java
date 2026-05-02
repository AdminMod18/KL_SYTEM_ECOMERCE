package com.marketplace.solicitud.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.solicitud.integration.ValidacionVendedorGateway;
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
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Panel director: listado filtrado sin adjuntos y orden por fecha descendente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SolicitudDirectorListadoIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ValidacionVendedorGateway validacionVendedorGateway;

    @MockBean
    SolicitudEstadoNotificador solicitudEstadoNotificador;

    @Test
    void listar_sinFiltros_devuelveItemsSinAdjuntos() throws Exception {
        String doc = "CC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        mockMvc.perform(postCrear(doc, "Lista Uno"))
                .andExpect(status().isCreated());

        MvcResult listado = mockMvc.perform(get("/solicitudes")).andExpect(status().isOk()).andReturn();

        JsonNode arr = objectMapper.readTree(listado.getResponse().getContentAsString());
        assertThat(arr.isArray()).isTrue();
        assertThat(arr.size()).isGreaterThanOrEqualTo(1);
        JsonNode primero = arr.get(0);
        assertThat(primero.has("adjuntos")).isFalse();
        assertThat(primero.has("estado")).isTrue();
        assertThat(primero.has("documentoIdentidad")).isTrue();
    }

    @Test
    void filtroPorEstado_yDocumento() throws Exception {
        String doc = "ZZ" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        MvcResult creada = mockMvc.perform(postCrear(doc, "Filtro QA")).andExpect(status().isCreated()).andReturn();
        long id = objectMapper.readTree(creada.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(
                        get("/solicitudes")
                                .queryParam("estado", "PENDIENTE")
                                .queryParam("documentoIdentidad", doc.substring(2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));

        MvcResult soloActivas =
                mockMvc.perform(get("/solicitudes").queryParam("estado", "ACTIVA")).andExpect(status().isOk()).andReturn();
        JsonNode arrActivas = objectMapper.readTree(soloActivas.getResponse().getContentAsString());
        boolean docEnActivas =
                StreamSupport.stream(arrActivas.spliterator(), false)
                        .anyMatch(n -> doc.equals(n.path("documentoIdentidad").asText()));
        assertThat(docEnActivas).isFalse();
    }

    private org.springframework.test.web.servlet.RequestBuilder postCrear(String doc, String nombreMostrar)
            throws com.fasterxml.jackson.core.JsonProcessingException {
        String body = CuerpoJsonSolicitud.crearNaturalNombreMostrar(objectMapper, doc, nombreMostrar, "Test", "User");
        return post("/solicitudes").contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
