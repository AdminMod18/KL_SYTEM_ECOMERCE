package com.marketplace.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Propósito: verificar arranque del Config Server y endpoint del Singleton.
 * Patrón: prueba de integración MVC ligera.
 * Responsabilidad: asegurar que el contexto carga y el singleton es consultable vía HTTP.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ConfigServiceApplicationTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void singletonMetadatos_ok() throws Exception {
        mockMvc.perform(get("/singleton/metadatos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreServicio").value("config-service"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }
}
