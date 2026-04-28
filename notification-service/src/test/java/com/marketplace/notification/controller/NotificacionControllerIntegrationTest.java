package com.marketplace.notification.controller;

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
 * Proposito: validar Observer Pattern para eventos COMPRA y SOLICITUD_APROBADA via API.
 * Patron: test MVC de integracion.
 * Responsabilidad: comprobar que se ejecuta un observer por tipo y retorna mensaje esperado.
 */
@SpringBootTest
@AutoConfigureMockMvc
class NotificacionControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void eventoCompra_disparaObserverCompra() throws Exception {
        String json = """
                {
                  "tipoEvento": "COMPRA",
                  "emailDestino": "cliente@demo.com",
                  "nombreActor": "Cliente Demo",
                  "referencia": "ORD-1001"
                }
                """;

        mockMvc.perform(post("/notificaciones/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoEvento").value("COMPRA"))
                .andExpect(jsonPath("$.observadoresEjecutados").value(1))
                .andExpect(jsonPath("$.mensajes[0]").exists());
    }

    @Test
    void eventoSolicitudAprobada_disparaObserverSolicitud() throws Exception {
        String json = """
                {
                  "tipoEvento": "SOLICITUD_APROBADA",
                  "emailDestino": "vendedor@demo.com",
                  "nombreActor": "Vendedor Demo",
                  "referencia": "SOL-777"
                }
                """;

        mockMvc.perform(post("/notificaciones/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoEvento").value("SOLICITUD_APROBADA"))
                .andExpect(jsonPath("$.observadoresEjecutados").value(1))
                .andExpect(jsonPath("$.mensajes[0]").exists());
    }
}
