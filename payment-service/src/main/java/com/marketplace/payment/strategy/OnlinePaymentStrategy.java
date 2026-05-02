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
 * <p>Token mágico {@value #TOKEN_SIMULAR_RECHAZO}: respuesta HTTP 201 con estado no autorizado (QA / demos sin pasarela real).
 */
@Component
public class OnlinePaymentStrategy implements PaymentStrategy {

    /** Si {@code tokenPasarela} coincide, se devuelve estado {@code DECLINADO} (200/201 + cuerpo, no excepción). */
    public static final String TOKEN_SIMULAR_RECHAZO = "tok_simular_rechazo";

    @Override
    public TipoPago tipo() {
        return TipoPago.ONLINE;
    }

    @Override
    public PagoResponse procesar(PagoRequest request) {
        if (request.getTokenPasarela() == null || request.getTokenPasarela().isBlank()) {
            throw new IllegalArgumentException("tokenPasarela es obligatorio para pagos ONLINE.");
        }
        if (TOKEN_SIMULAR_RECHAZO.equals(request.getTokenPasarela().trim())) {
            return new PagoResponse(
                    UUID.randomUUID(),
                    "DECLINADO",
                    "Pasarela ONLINE (simulación): pago no autorizado.",
                    TipoPago.ONLINE);
        }
        return new PagoResponse(
                UUID.randomUUID(),
                "AUTORIZADO",
                "Pasarela ONLINE: cargo simulado correctamente.",
                TipoPago.ONLINE);
    }
}
