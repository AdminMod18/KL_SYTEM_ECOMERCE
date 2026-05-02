package com.marketplace.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("admin-service")
                        .description("Parametrizacion del sistema, auditoria y logs (caso estudio — datos demo).")
                        .version("1.0.0")
                        .contact(new Contact().name("Marketplace").email("soporte@example.com")));
    }
}
