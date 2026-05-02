package com.marketplace.solicitud.dto;

import com.marketplace.solicitud.model.PeriodoSuscripcionPlan;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Comando para activar vendedor tras cobro: se reenvía a {@code POST /pagos} del payment-service.
 */
@Getter
@Setter
public class ActivacionVendedorRequest {

    @NotNull
    private TipoPagoActivacion tipo;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true, message = "El monto debe ser mayor a cero.")
    private BigDecimal monto;

    /**
     * Plan contratado (mensual / semestral / anual). Si es null se usa el periodo por defecto de configuración.
     */
    private PeriodoSuscripcionPlan periodoSuscripcion;

    /**
     * Referencia de negocio; si viene vacío se usa {@code SOLICITUD-{id}}.
     */
    @Size(max = 120)
    private String referenciaCliente;

    @Size(max = 128)
    private String tokenPasarela;

    @Size(max = 4)
    private String ultimosDigitosTarjeta;

    @Size(max = 64)
    private String numeroComprobanteConsignacion;
}
