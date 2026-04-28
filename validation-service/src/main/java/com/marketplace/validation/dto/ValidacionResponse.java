package com.marketplace.validation.dto;

import java.util.List;

/**
 * Propósito: respuesta agregada tras ejecutar la fachada de validación.
 * Patrón: DTO de salida / Aggregate view.
 * Responsabilidad: exponer decisión {@code apto} y desgloses trazables para auditoría.
 */
public record ValidacionResponse(
        boolean apto,
        int scoreDatacredito,
        boolean listaControlDatacredito,
        String referenciaConsultaDatacredito,
        int indicadorRiesgoCifin,
        String estadoLineaCifin,
        boolean lineaCifinEncontrada,
        List<String> observaciones
) {
}
