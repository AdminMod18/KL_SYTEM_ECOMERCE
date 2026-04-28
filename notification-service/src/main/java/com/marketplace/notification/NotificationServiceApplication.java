package com.marketplace.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Proposito: arranque del microservicio de notificaciones orientado a eventos.
 * Patron: Bootstrap Spring Boot.
 * Responsabilidad: iniciar el contexto con subject y observers de notificacion.
 */
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
