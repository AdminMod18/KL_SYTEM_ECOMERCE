package com.marketplace.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Propósito: verificar arranque del contexto Spring Boot de auth-service.
 * Patrón: smoke test.
 * Responsabilidad: detectar errores de wiring (proxy, real, JWT).
 */
@SpringBootTest
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
