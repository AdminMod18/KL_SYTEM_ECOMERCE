package com.marketplace.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Propósito: arranque del microservicio de catálogo de productos.
 * Patrón: Bootstrap Spring Boot.
 * Responsabilidad: inicializar contexto con builder de productos y árbol composite de categorías.
 */
@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
