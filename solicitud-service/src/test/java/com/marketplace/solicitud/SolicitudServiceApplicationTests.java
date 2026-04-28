package com.marketplace.solicitud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Propósito: verificar que el contexto Spring arranca con la configuración por defecto.
 * Patrón: Smoke test de integración ligera.
 * Responsabilidad: fallar en CI si faltan beans o configuración JPA.
 */
@SpringBootTest
class SolicitudServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
