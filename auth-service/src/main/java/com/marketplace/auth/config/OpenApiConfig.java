package com.marketplace.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Propósito: centralizar metadatos OpenAPI y el esquema de seguridad JWT para este microservicio.
 * Patrón: Configuration Object (Spring {@code @Configuration} + bean factory).
 * Responsabilidad: definir el bean {@link OpenAPI} consumido por springdoc (Swagger UI y {@code /v3/api-docs}).
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_BEARER = "bearer-jwt";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("auth-service")
                        .description("Autenticación: login y extracción de roles desde JWT.")
                        .version("1.0.0")
                        .contact(new Contact().name("Marketplace").email("soporte@example.com")))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_BEARER,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_BEARER)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token emitido por POST /auth/login")));
    }
}
