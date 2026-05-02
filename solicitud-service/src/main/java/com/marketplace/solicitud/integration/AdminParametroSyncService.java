package com.marketplace.solicitud.integration;

import com.marketplace.solicitud.config.AdminIntegrationProperties;
import com.marketplace.solicitud.config.SolicitudParametrosOperativos;
import com.marketplace.solicitud.integration.dto.AdminParametroRemotoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Obtiene parámetros desde admin-service y actualiza {@link SolicitudParametrosOperativos}.
 */
@Service
public class AdminParametroSyncService {

    private static final Logger log = LoggerFactory.getLogger(AdminParametroSyncService.class);

    private final AdminIntegrationProperties adminIntegrationProperties;
    private final RestTemplate adminRestTemplate;
    private final SolicitudParametrosOperativos solicitudParametrosOperativos;

    public AdminParametroSyncService(
            AdminIntegrationProperties adminIntegrationProperties,
            @Qualifier("adminRestTemplate") RestTemplate adminRestTemplate,
            SolicitudParametrosOperativos solicitudParametrosOperativos) {
        this.adminIntegrationProperties = adminIntegrationProperties;
        this.adminRestTemplate = adminRestTemplate;
        this.solicitudParametrosOperativos = solicitudParametrosOperativos;
    }

    @PostConstruct
    void sincronizarAlArrancar() {
        sincronizarSiCorresponde();
    }

    public void sincronizarSiCorresponde() {
        String base = adminIntegrationProperties.getBaseUrl();
        if (base == null || base.isBlank()) {
            return;
        }
        String url =
                base.endsWith("/") ? base + "admin/parametros" : base + "/admin/parametros";
        try {
            ResponseEntity<List<AdminParametroRemotoDto>> resp =
                    adminRestTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<AdminParametroRemotoDto>>() {});
            List<AdminParametroRemotoDto> body = resp.getBody();
            if (body == null || body.isEmpty()) {
                log.debug("admin-service devolvió lista vacía de parámetros");
                return;
            }
            Map<String, String> map = new LinkedHashMap<>();
            for (AdminParametroRemotoDto row : body) {
                if (row.clave() != null && row.valor() != null) {
                    map.put(row.clave().trim(), row.valor().trim());
                }
            }
            solicitudParametrosOperativos.aplicarValoresAdminSiPresentes(map);
            log.info("Parámetros operativos actualizados desde admin-service ({} filas)", body.size());
        } catch (RestClientException ex) {
            log.warn("No se pudo sincronizar parámetros desde admin-service: {}", ex.getMessage());
        }
    }
}
