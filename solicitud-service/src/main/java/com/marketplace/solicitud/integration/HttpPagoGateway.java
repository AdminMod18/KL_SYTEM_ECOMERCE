package com.marketplace.solicitud.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.marketplace.solicitud.dto.ActivacionVendedorRequest;
import com.marketplace.solicitud.dto.TipoPagoActivacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Cliente HTTP hacia {@code POST {base-url}/pagos} del payment-service.
 */
@Component
public class HttpPagoGateway implements PagoGateway {

    private static final Logger log = LoggerFactory.getLogger(HttpPagoGateway.class);

    private final RestTemplate restTemplate;
    private final String paymentBaseUrl;

    public HttpPagoGateway(
            @Qualifier("paymentRestTemplate") RestTemplate restTemplate,
            @Value("${integracion.payment.base-url:}") String paymentBaseUrl) {
        this.restTemplate = restTemplate;
        this.paymentBaseUrl = paymentBaseUrl == null ? "" : paymentBaseUrl.trim();
    }

    @Override
    public PagoRemotoResult procesarPago(ActivacionVendedorRequest request, long solicitudId) {
        if (paymentBaseUrl.isBlank()) {
            throw new IllegalStateException(
                    "Configure integracion.payment.base-url (URL base del payment-service, sin barra final).");
        }
        URI url = URI.create(trimTrailingSlashes(paymentBaseUrl) + "/pagos");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tipo", request.getTipo().name());
        body.put("monto", request.getMonto());
        String ref = request.getReferenciaCliente();
        if (ref == null || ref.isBlank()) {
            ref = "SOLICITUD-" + solicitudId;
        }
        body.put("referenciaCliente", ref);
        if (request.getTipo() == TipoPagoActivacion.ONLINE) {
            body.put("tokenPasarela", request.getTokenPasarela());
        }
        if (request.getTipo() == TipoPagoActivacion.TARJETA) {
            body.put("ultimosDigitosTarjeta", request.getUltimosDigitosTarjeta());
        }
        if (request.getTipo() == TipoPagoActivacion.CONSIGNACION) {
            body.put("numeroComprobanteConsignacion", request.getNumeroComprobanteConsignacion());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        log.info("Invocando payment-service POST {}", url);

        try {
            ResponseEntity<PagoHttpResponse> resp =
                    restTemplate.postForEntity(url, entity, PagoHttpResponse.class);
            if (resp.getBody() == null || resp.getBody().estado() == null || resp.getBody().estado().isBlank()) {
                throw new IllegalStateException("Respuesta de payment-service vacía o sin estado.");
            }
            String estado = resp.getBody().estado().trim();
            log.info("payment-service respondió HTTP {} estado={} idTransaccion={}", resp.getStatusCode().value(), estado, resp.getBody().idTransaccion());
            return new PagoRemotoResult(
                    resp.getBody().idTransaccion(),
                    estado,
                    resp.getBody().mensaje(),
                    resp.getBody().tipo() != null ? resp.getBody().tipo() : request.getTipo().name());
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new IllegalStateException(
                    "payment-service respondió HTTP "
                            + ex.getStatusCode().value()
                            + ": "
                            + ex.getResponseBodyAsString(),
                    ex);
        } catch (ResourceAccessException ex) {
            throw new IllegalStateException(
                    "Timeout o error de red al contactar payment-service (" + url.getHost() + "): " + ex.getMessage(),
                    ex);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Fallo llamando al servicio de pago: " + ex.getMessage(), ex);
        }
    }

    private static String trimTrailingSlashes(String url) {
        String s = url.trim();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PagoHttpResponse(UUID idTransaccion, String estado, String mensaje, String tipo) {
    }
}
