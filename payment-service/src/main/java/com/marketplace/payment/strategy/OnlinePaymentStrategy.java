package com.marketplace.payment.strategy;

import com.marketplace.payment.dto.PagoRequest;
import com.marketplace.payment.dto.PagoResponse;
import com.marketplace.payment.model.TipoPago;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Propósito: simular cobro vía pasarela en línea (PSP) usando un token de sesión.
 * Patrón: Strategy concreta.
 * Responsabilidad: verificar token y emitir autorización simulada para pagos ONLINE.
 */
@Component
public class OnlinePaymentStrategy implements PaymentStrategy {

    @Override
    public TipoPago tipo() {
        return TipoPago.ONLINE;
    }

    @Override
    public PagoResponse procesar(PagoRequest request) {
        if (request.getTokenPasarela() == null || request.getTokenPasarela().isBlank()) {
            throw new IllegalArgumentException("tokenPasarela es obligatorio para pagos ONLINE.");
        }
        return new PagoResponse(
                UUID.randomUUID(),
                "AUTORIZADO",
                "Pasarela ONLINE: cargo simulado correctamente.",
                TipoPago.ONLINE);
    }
}
