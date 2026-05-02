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
    void eventoSolicitudMora_disparaObserver() throws Exception {
        String json = """
                {
                  "tipoEvento": "SOLICITUD_MORA",
                  "emailDestino": "mora@demo.com",
                  "nombreActor": "Vendedor Mora",
                  "referencia": "SOL-1|vence=2026-01-01T00:00:00Z"
                }
                """;

        mockMvc.perform(post("/notificaciones/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoEvento").value("SOLICITUD_MORA"))
                .andExpect(jsonPath("$.observadoresEjecutados").value(1))
                .andExpect(jsonPath("$.mensajes[0]").exists());
    }

    @Test
    void eventoCancelacionSuscripcion_disparaObserver() throws Exception {
        String json = """
                {
                  "tipoEvento": "SOLICITUD_CANCELACION_SUSCRIPCION",
                  "emailDestino": "baja@demo.com",
                  "nombreActor": "Vendedor Baja",
                  "referencia": "SOL-2|vence=2026-01-01T00:00:00Z"
                }
                """;

        mockMvc.perform(post("/notificaciones/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoEvento").value("SOLICITUD_CANCELACION_SUSCRIPCION"))
                .andExpect(jsonPath("$.observadoresEjecutados").value(1))
                .andExpect(jsonPath("$.mensajes[0]").exists());
    }

    @Test
    void eventoCancelacionReputacion_disparaObserver() throws Exception {
        String json = """
                {
                  "tipoEvento": "SOLICITUD_CANCELACION_REPUTACION",
                  "emailDestino": "baja-rep@demo.com",
                  "nombreActor": "Vendedor Rep",
                  "referencia": "SOL-9|malas=10"
                }
                """;

        mockMvc.perform(post("/notificaciones/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoEvento").value("SOLICITUD_CANCELACION_REPUTACION"))
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
