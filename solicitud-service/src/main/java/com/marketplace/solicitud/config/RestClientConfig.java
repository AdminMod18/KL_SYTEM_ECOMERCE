package com.marketplace.solicitud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * {@link RestTemplate} por integración saliente (validation-service, payment-service, notification-service opcional).
 */
@Configuration
public class RestClientConfig {

    @Bean(name = "notificationRestTemplate")
    public RestTemplate notificationRestTemplate(
            RestTemplateBuilder builder,
            @Value("${integracion.notification.connect-timeout:5s}") Duration connectTimeout,
            @Value("${integracion.notification.read-timeout:15s}") Duration readTimeout) {
        return builder.setConnectTimeout(connectTimeout).setReadTimeout(readTimeout).build();
    }

    @Bean(name = "validationRestTemplate")
    public RestTemplate validationRestTemplate(
            RestTemplateBuilder builder,
            @Value("${integracion.validation.connect-timeout:5s}") Duration connectTimeout,
            @Value("${integracion.validation.read-timeout:30s}") Duration readTimeout) {
        return builder
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(readTimeout)
                .build();
    }

    @Bean(name = "paymentRestTemplate")
    public RestTemplate paymentRestTemplate(
            RestTemplateBuilder builder,
            @Value("${integracion.payment.connect-timeout:5s}") Duration connectTimeout,
            @Value("${integracion.payment.read-timeout:30s}") Duration readTimeout) {
        return builder
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(readTimeout)
                .build();
    }

    @Bean(name = "adminRestTemplate")
    public RestTemplate adminRestTemplate(
            RestTemplateBuilder builder,
            @Value("${integracion.admin.connect-timeout:5s}") Duration connectTimeout,
            @Value("${integracion.admin.read-timeout:15s}") Duration readTimeout) {
        return builder.setConnectTimeout(connectTimeout).setReadTimeout(readTimeout).build();
    }

    @Bean(name = "userServiceRestTemplate")
    public RestTemplate userServiceRestTemplate(
            RestTemplateBuilder builder,
            @Value("${integracion.user-service.connect-timeout:5s}") Duration connectTimeout,
            @Value("${integracion.user-service.read-timeout:15s}") Duration readTimeout) {
        return builder.setConnectTimeout(connectTimeout).setReadTimeout(readTimeout).build();
    }
}
