package com.marketplace.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Propósito: arranque del servidor de configuración central (Spring Cloud Config).
 * Patrón: bootstrap Spring Boot + Config Server.
 * Responsabilidad: exponer API de configuración y metadatos del singleton de identidad del servicio.
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServiceApplication.class, args);
    }
}
