package com.marketplace.solicitud.dto;

/**
 * Vista pública de reputación por solicitud de vendedor (HU-17).
 */
public record ReputacionResumenResponse(Long solicitudId, long totalCalificaciones, Double promedioValor) {}
