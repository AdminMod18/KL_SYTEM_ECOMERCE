package com.marketplace.validation.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Propósito: registrar beans HTTP para adaptadores que consumen APIs externas (mock Datacrédito).
 * Patrón: Singleton de infraestructura (bean único {@link RestTemplate} por contexto).
 * Responsabilidad: centralizar timeouts y defaults del cliente HTTP.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
