package com.marketplace.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Propósito: arranque del microservicio de gestión de usuarios del marketplace.
 * Patrón: bootstrap Spring Boot.
 * Responsabilidad: inicializar contexto con API CRUD y persistencia JPA.
 */
@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
