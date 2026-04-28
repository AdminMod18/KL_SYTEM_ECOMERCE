package com.marketplace.payment.strategy;

import com.marketplace.payment.dto.PagoRequest;
import com.marketplace.payment.dto.PagoResponse;
import com.marketplace.payment.model.TipoPago;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Propósito: simular captura con tarjeta presente/no presente retén de últimos dígitos.
 * Patrón: Strategy concreta.
 * Responsabilidad: validar máscara numérica y marcar transacción como capturada en modo tarjeta.
 */
@Component
public class TarjetaPaymentStrategy implements PaymentStrategy {

    private static final Pattern DIGITOS = Pattern.compile("^\\d{4}$");

    @Override
    public TipoPago tipo() {
        return TipoPago.TARJETA;
    }

    @Override
    public PagoResponse procesar(PagoRequest request) {
        String ultimos = request.getUltimosDigitosTarjeta();
        if (ultimos == null || !DIGITOS.matcher(ultimos).matches()) {
            throw new IllegalArgumentException("ultimosDigitosTarjeta debe tener exactamente 4 dígitos.");
        }
        return new PagoResponse(
                UUID.randomUUID(),
                "CAPTURADO",
                "Adquirente simulado aprobó la tarjeta terminada en " + ultimos + ".",
                TipoPago.TARJETA);
    }
}
