package com.marketplace.solicitud.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

/**
 * Contrato JSON de {@code GET /admin/parametros} del admin-service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminParametroRemotoDto(String clave, String valor, Instant actualizadoEn) {}
