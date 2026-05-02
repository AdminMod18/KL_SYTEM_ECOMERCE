package com.marketplace.solicitud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "integracion.admin")
public class AdminIntegrationProperties {

    /**
     * Si está vacío, no se consulta admin-service (solo YAML local).
     */
    private String baseUrl = "";

    private long syncFixedDelayMs = 300_000L;

    private Duration connectTimeout = Duration.ofSeconds(5);

    private Duration readTimeout = Duration.ofSeconds(15);

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public long getSyncFixedDelayMs() {
        return syncFixedDelayMs;
    }

    public void setSyncFixedDelayMs(long syncFixedDelayMs) {
        this.syncFixedDelayMs = syncFixedDelayMs;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }
}
