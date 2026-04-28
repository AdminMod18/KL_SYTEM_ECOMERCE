package com.marketplace.payment.strategy;

import com.marketplace.payment.dto.PagoRequest;
import com.marketplace.payment.dto.PagoResponse;
import com.marketplace.payment.model.TipoPago;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Propósito: registrar pagos por consignación bancaria contrastando el número de comprobante.
 * Patrón: Strategy concreta.
 * Responsabilidad: validar comprobante y dejar constancia en estado PENDIENTE_VERIFICACION o RECIBIDO según reglas mock.
 */
@Component
public class ConsignacionPaymentStrategy implements PaymentStrategy {

    @Override
    public TipoPago tipo() {
        return TipoPago.CONSIGNACION;
    }

    @Override
    public PagoResponse procesar(PagoRequest request) {
        String comprobante = request.getNumeroComprobanteConsignacion();
        if (comprobante == null || comprobante.isBlank()) {
            throw new IllegalArgumentException("numeroComprobanteConsignacion es obligatorio para CONSIGNACION.");
        }
        return new PagoResponse(
                UUID.randomUUID(),
                "RECIBIDO",
                "Consignación registrada; comprobante " + comprobante + " en validación back-office.",
                TipoPago.CONSIGNACION);
    }
}
