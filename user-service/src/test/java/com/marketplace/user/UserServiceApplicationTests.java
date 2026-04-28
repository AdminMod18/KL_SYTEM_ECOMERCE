package com.marketplace.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Propósito: verificar arranque del contexto Spring Boot del user-service.
 * Patrón: smoke test.
 * Responsabilidad: detectar fallos de wiring en CI.
 */
@SpringBootTest
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
