package com.marketplace.user.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Propósito: validar flujo CRUD completo de usuarios vía REST.
 * Patrón: prueba MVC de integración.
 * Responsabilidad: comprobar creación, lectura, actualización y borrado.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void crudUsuario() throws Exception {
        String crear = """
                {"nombreUsuario":"hugo","email":"hugo@demo.com","nombreCompleto":"Hugo Demo"}
                """;

        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(crear))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombreUsuario").value("hugo"));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("hugo@demo.com"));

        String actualizar = """
                {"nombreUsuario":"hugo2","email":"hugo2@demo.com","nombreCompleto":"Hugo Actualizado"}
                """;

        mockMvc.perform(put("/usuarios/1").contentType(MediaType.APPLICATION_JSON).content(actualizar))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("hugo2"));

        mockMvc.perform(delete("/usuarios/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
