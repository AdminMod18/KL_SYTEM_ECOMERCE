package com.marketplace.solicitud;

import com.marketplace.solicitud.config.AdminIntegrationProperties;
import com.marketplace.solicitud.config.ReputacionProperties;
import com.marketplace.solicitud.config.SuscripcionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Propósito: punto de entrada del microservicio de solicitudes de registro de vendedores.
 * Patrón: ninguno (Bootstrap Spring Boot).
 * Responsabilidad: arrancar el contexto de aplicación y registrar componentes.
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
    SuscripcionProperties.class,
    ReputacionProperties.class,
    AdminIntegrationProperties.class
})
public class SolicitudServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SolicitudServiceApplication.class, args);
    }
}
