package com.marketplace.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Propósito: arranque del microservicio de cobros del marketplace.
 * Patrón: bootstrap estándar Spring Boot (sin patrón GoF aquí).
 * Responsabilidad: cargar el contexto de aplicación con estrategias de pago y la fábrica asociada.
 */
@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
