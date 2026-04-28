package com.marketplace.payment.service;

import com.marketplace.payment.dto.PagoRequest;
import com.marketplace.payment.dto.PagoResponse;
import com.marketplace.payment.factory.PaymentStrategyFactory;
import com.marketplace.payment.strategy.PaymentStrategy;
import org.springframework.stereotype.Service;

/**
 * Propósito: caso de uso de registro de pagos del marketplace.
 * Patrón: Application Service (orquesta Factory Method + Strategy sin conocer detalles de canal).
 * Responsabilidad: delegar en la fábrica la selección de estrategia y devolver el resultado al controlador.
 */
@Service
public class PagoService {

    private final PaymentStrategyFactory paymentStrategyFactory;

    public PagoService(PaymentStrategyFactory paymentStrategyFactory) {
        this.paymentStrategyFactory = paymentStrategyFactory;
    }

    public PagoResponse procesarPago(PagoRequest request) {
        PaymentStrategy estrategia = paymentStrategyFactory.obtenerEstrategia(request.getTipo());
        return estrategia.procesar(request);
    }
}
