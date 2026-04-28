package com.marketplace.solicitud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Propósito: punto de entrada del microservicio de solicitudes de registro de vendedores.
 * Patrón: ninguno (Bootstrap Spring Boot).
 * Responsabilidad: arrancar el contexto de aplicación y registrar componentes.
 */
@SpringBootApplication
public class SolicitudServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SolicitudServiceApplication.class, args);
    }
}
