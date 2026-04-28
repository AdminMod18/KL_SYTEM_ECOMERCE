package com.marketplace.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Propósito: validar login y extracción de roles con JWT.
 * Patrón: prueba MVC de integración.
 * Responsabilidad: comprobar flujo completo vía {@link AuthController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void loginYRoles_conAdmin() throws Exception {
        String body = """
                {"username":"admin","password":"admin123"}
                """;

        MvcResult login = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        JsonNode json = objectMapper.readTree(login.getResponse().getContentAsString());
        String token = json.get("accessToken").asText();

        MvcResult roles = mockMvc.perform(get("/auth/roles").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles").isArray())
                .andReturn();

        JsonNode rolesJson = objectMapper.readTree(roles.getResponse().getContentAsString());
        assertThat(rolesJson.get("roles").toString()).contains("ADMIN");
    }
}
