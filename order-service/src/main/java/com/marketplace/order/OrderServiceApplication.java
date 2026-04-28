package com.marketplace.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Propósito: arranque del microservicio de órdenes de compra del marketplace.
 * Patrón: bootstrap Spring Boot (sin patrón GoF en esta clase).
 * Responsabilidad: cargar contexto con comandos, invocador y cadena decorada de precios.
 */
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
