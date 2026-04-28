package com.marketplace.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Propósito: arranque del microservicio de autenticación y autorización.
 * Patrón: bootstrap Spring Boot.
 * Responsabilidad: iniciar el contexto con proxy de autenticación y emisión JWT.
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
