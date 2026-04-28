package com.marketplace.analytics.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Propósito: metadatos OpenAPI del microservicio de analítica.
 * Patrón: Configuration Object (Spring {@code @Configuration} + bean factory).
 * Responsabilidad: exponer el bean {@link OpenAPI} para springdoc.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("analytics-service")
                        .description("Ingesta de métricas y consulta de KPIs agregados.")
                        .version("1.0.0")
                        .contact(new Contact().name("Marketplace").email("soporte@example.com")));
    }
}
