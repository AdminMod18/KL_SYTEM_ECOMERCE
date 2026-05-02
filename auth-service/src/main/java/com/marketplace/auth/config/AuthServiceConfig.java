package com.marketplace.auth.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Propósito: módulo de configuración del auth-service.
 * Patrón: configuración Spring.
 * Responsabilidad: punto de extensión para beans adicionales de seguridad.
 */
@Configuration
public class AuthServiceConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
