package com.marketplace.order;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Propósito: verificar que el contexto Spring Boot arranca con JPA y beans de precios.
 * Patrón: smoke test de integración.
 * Responsabilidad: fallar en CI si hay errores de cableado.
 */
@SpringBootTest
class OrderServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
