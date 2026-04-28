package com.marketplace.payment.dto;

import com.marketplace.payment.model.TipoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Propósito: comando REST para iniciar un cobro (campos comunes + específicos opcionales por tipo).
 * Patrón: DTO de entrada / comando.
 * Responsabilidad: transportar datos validados hacia la estrategia correspondiente sin exponer entidades internas.
 */
@Getter
@Setter
public class PagoRequest {

    @NotNull
    private TipoPago tipo;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true, message = "El monto debe ser mayor a cero.")
    private BigDecimal monto;

    @NotBlank
    @Size(max = 120)
    private String referenciaCliente;

    /** Obligatorio si tipo == ONLINE (token de sesión simulado de pasarela). */
    @Size(max = 128)
    private String tokenPasarela;

    /** Obligatorio si tipo == TARJETA (últimos dígitos). */
    @Size(max = 4)
    private String ultimosDigitosTarjeta;

    /** Obligatorio si tipo == CONSIGNACION (referencia bancaria). */
    @Size(max = 64)
    private String numeroComprobanteConsignacion;
}
