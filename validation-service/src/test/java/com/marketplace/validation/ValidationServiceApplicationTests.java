package com.marketplace.validation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Propósito: comprobar arranque del contexto Spring Boot.
 * Patrón: smoke test de integración.
 * Responsabilidad: fallar en CI si falta configuración de beans.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ValidationServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
