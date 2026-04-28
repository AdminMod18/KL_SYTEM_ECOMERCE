package com.marketplace.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Propósito: arranque del microservicio de analítica y KPIs del marketplace.
 * Patrón: bootstrap Spring Boot.
 * Responsabilidad: inicializar persistencia de eventos y exposición REST de agregados.
 */
@SpringBootApplication
public class AnalyticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }
}
