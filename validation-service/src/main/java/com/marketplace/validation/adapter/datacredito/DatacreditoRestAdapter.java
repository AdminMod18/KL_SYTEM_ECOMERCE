package com.marketplace.validation.adapter.datacredito;

import com.marketplace.validation.domain.ResultadoDatacredito;
import com.marketplace.validation.port.DatacreditoConsultaPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Propósito: adaptar la API REST de Datacrédito (aquí mock HTTP interno) al modelo de dominio del marketplace.
 * Patrón: Adapter (convierte contrato externo REST + JSON a {@link ResultadoDatacredito} estable).
 * Responsabilidad: construir URI, invocar GET y mapear errores en fallos de integración claros.
 */
@Component
public class DatacreditoRestAdapter implements DatacreditoConsultaPort {

    private final RestTemplate restTemplate;
    private final ServletWebServerApplicationContext servletWebServerApplicationContext;
    private final String datacreditoBaseUrlExplicito;

    public DatacreditoRestAdapter(
            RestTemplate restTemplate,
            ServletWebServerApplicationContext servletWebServerApplicationContext,
            @Value("${validation.datacredito.base-url:}") String datacreditoBaseUrlExplicito) {
        this.restTemplate = restTemplate;
        this.servletWebServerApplicationContext = servletWebServerApplicationContext;
        this.datacreditoBaseUrlExplicito = datacreditoBaseUrlExplicito;
    }

    @Override
    public ResultadoDatacredito consultarPorDocumento(String documentoIdentidad) {
        String base = resolverBaseUrl();
        String uri = UriComponentsBuilder
                .fromUriString(base)
                .path("/internal/mock/datacredito/{documento}")
                .buildAndExpand(documentoIdentidad)
                .toUriString();
        try {
            DatacreditoMockDto dto = restTemplate.getForObject(uri, DatacreditoMockDto.class);
            if (dto == null) {
                throw new IllegalStateException("Respuesta vacía de Datacrédito mock.");
            }
            return new ResultadoDatacredito(dto.score(), dto.listaControl(), dto.referenciaConsulta());
        } catch (RestClientException ex) {
            throw new IllegalStateException("Fallo consultando Datacrédito mock: " + ex.getMessage(), ex);
        }
    }

    private String resolverBaseUrl() {
        if (datacreditoBaseUrlExplicito != null && !datacreditoBaseUrlExplicito.isBlank()) {
            return datacreditoBaseUrlExplicito;
        }
        int port = servletWebServerApplicationContext.getWebServer().getPort();
        return "http://127.0.0.1:" + port;
    }

    /**
     * Forma esperada del JSON REST mock (contrato externo adaptado).
     */
    public record DatacreditoMockDto(
            String documento,
            int score,
            boolean listaControl,
            String referenciaConsulta
    ) {
    }
}
