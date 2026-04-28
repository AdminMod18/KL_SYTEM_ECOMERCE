package com.marketplace.validation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Propósito: arranque del microservicio de validaciones crediticias y de archivo CIFIN.
 * Patrón: Bootstrap estándar Spring Boot (sin patrón GoF aplicado aquí).
 * Responsabilidad: inicializar el contexto de aplicación y exponer REST internos mock y públicos.
 */
@SpringBootApplication
public class ValidationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValidationServiceApplication.class, args);
    }
}
