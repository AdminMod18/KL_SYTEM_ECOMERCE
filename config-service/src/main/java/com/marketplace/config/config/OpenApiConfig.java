package com.marketplace.config.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Propósito: metadatos OpenAPI del config-server y endpoints auxiliares de demostración.
 * Patrón: Configuration Object (Spring {@code @Configuration} + bean factory).
 * Responsabilidad: exponer el bean {@link OpenAPI} para springdoc junto a Spring Cloud Config.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("config-service")
                        .description("Spring Cloud Config (perfil native) y metadatos del patrón Singleton de registro.")
                        .version("1.0.0")
                        .contact(new Contact().name("Marketplace").email("soporte@example.com")));
    }
}
