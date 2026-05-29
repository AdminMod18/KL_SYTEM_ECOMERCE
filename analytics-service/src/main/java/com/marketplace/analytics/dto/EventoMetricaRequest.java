package com.marketplace.analytics.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.marketplace.analytics.model.TipoEventoMetrica;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Propósito: comando REST para registrar un hecho que alimenta KPIs.
 * Patrón: DTO de entrada.
 * Responsabilidad: transportar tipo, referencia y valor monetario opcional (p. ej. total de compra).
 */
@Getter
@Setter
public class EventoMetricaRequest {

    @NotNull
    @JsonAlias({"type"})
    private TipoEventoMetrica tipo;

    @NotBlank
    @Size(max = 120)
    @JsonAlias({"reference", "sku", "id"})
    private String referencia;

    @DecimalMin(value = "0.0", inclusive = true)
    @JsonAlias({"valor", "amount", "total", "revenue"})
    private BigDecimal valorMonetario;
}
