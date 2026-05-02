package com.marketplace.payment.service;

import com.marketplace.payment.dto.PagoRequest;
import com.marketplace.payment.dto.PagoResponse;
import com.marketplace.payment.factory.PaymentStrategyFactory;
import com.marketplace.payment.strategy.PaymentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Propósito: caso de uso de registro de pagos del marketplace.
 * Patrón: Application Service (orquesta Factory Method + Strategy sin conocer detalles de canal).
 * Responsabilidad: delegar en la fábrica la selección de estrategia y devolver el resultado al controlador.
 */
@Service
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    private final PaymentStrategyFactory paymentStrategyFactory;

    public PagoService(PaymentStrategyFactory paymentStrategyFactory) {
        this.paymentStrategyFactory = paymentStrategyFactory;
    }

    public PagoResponse procesarPago(PagoRequest request) {
        PaymentStrategy estrategia = paymentStrategyFactory.obtenerEstrategia(request.getTipo());
        PagoResponse respuesta = estrategia.procesar(request);
        log.info(
                "Pago procesado referencia={} tipo={} estado={} idTransaccion={}",
                request.getReferenciaCliente(),
                request.getTipo(),
                respuesta.estado(),
                respuesta.idTransaccion());
        return respuesta;
    }
}
