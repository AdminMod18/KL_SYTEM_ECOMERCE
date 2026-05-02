package com.marketplace.payment.controller;

import com.marketplace.payment.strategy.OnlinePaymentStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Propósito: validar `POST /pagos` para los tres tipos de cobro simulados.
 * Patrón: prueba MVC de integración ligera.
 * Responsabilidad: comprobar que Factory + Strategy devuelven 201 y cuerpo esperado.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PagoControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void pagoOnline_ok() throws Exception {
        String json = """
                {"tipo":"ONLINE","monto":99.90,"referenciaCliente":"PED-1","tokenPasarela":"tok_demo"}
                """;
        mockMvc.perform(post("/pagos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("AUTORIZADO"))
                .andExpect(jsonPath("$.tipo").value("ONLINE"));
    }

    @Test
    void pagoTarjeta_ok() throws Exception {
        String json = """
                {"tipo":"TARJETA","monto":40,"referenciaCliente":"PED-2","ultimosDigitosTarjeta":"4242"}
                """;
        mockMvc.perform(post("/pagos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CAPTURADO"))
                .andExpect(jsonPath("$.mensaje").value(containsString("4242")));
    }

    @Test
    void pagoConsignacion_ok() throws Exception {
        String json = """
                {"tipo":"CONSIGNACION","monto":250,"referenciaCliente":"PED-3","numeroComprobanteConsignacion":"CSG-778899"}
                """;
        mockMvc.perform(post("/pagos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("RECIBIDO"))
                .andExpect(jsonPath("$.tipo").value("CONSIGNACION"));
    }

    @Test
    void pagoOnline_sinToken_badRequest() throws Exception {
        String json = """
                {"tipo":"ONLINE","monto":10,"referenciaCliente":"PED-4"}
                """;
        mockMvc.perform(post("/pagos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pagoOnline_tokenSimularRechazo_devuelveDeclinado() throws Exception {
        String json =
                """
                {"tipo":"ONLINE","monto":10,"referenciaCliente":"QA-DECL","tokenPasarela":"%s"}
                """
                        .formatted(OnlinePaymentStrategy.TOKEN_SIMULAR_RECHAZO);
        mockMvc.perform(post("/pagos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("DECLINADO"))
                .andExpect(jsonPath("$.tipo").value("ONLINE"));
    }
}
