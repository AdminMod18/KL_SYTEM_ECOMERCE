package com.marketplace.product.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate solicitudCatalogoRestTemplate(
            RestTemplateBuilder builder,
            @Value("${integracion.solicitud.connect-timeout:5s}") Duration connectTimeout,
            @Value("${integracion.solicitud.read-timeout:10s}") Duration readTimeout) {
        return builder.setConnectTimeout(connectTimeout).setReadTimeout(readTimeout).build();
    }
}
