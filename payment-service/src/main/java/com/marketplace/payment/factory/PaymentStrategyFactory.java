package com.marketplace.payment.factory;

import com.marketplace.payment.model.TipoPago;
import com.marketplace.payment.strategy.PaymentStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Propósito: crear (resolver) la estrategia de pago adecuada según el tipo solicitado.
 * Patrón: Factory Method (método factoría {@link #obtenerEstrategia(TipoPago)} sobre productos Strategy).
 * Responsabilidad: centralizar el mapa tipo → implementación y fallar de forma explícita si no hay soporte.
 */
@Component
public class PaymentStrategyFactory {

    private final Map<TipoPago, PaymentStrategy> estrategiasPorTipo;

    public PaymentStrategyFactory(List<PaymentStrategy> estrategias) {
        Map<TipoPago, PaymentStrategy> mapa = new EnumMap<>(TipoPago.class);
        for (PaymentStrategy estrategia : estrategias) {
            if (mapa.put(estrategia.tipo(), estrategia) != null) {
                throw new IllegalStateException("Estrategia duplicada para el tipo " + estrategia.tipo());
            }
        }
        this.estrategiasPorTipo = Map.copyOf(mapa);
    }

    public PaymentStrategy obtenerEstrategia(TipoPago tipo) {
        PaymentStrategy estrategia = estrategiasPorTipo.get(tipo);
        if (estrategia == null) {
            throw new IllegalArgumentException("Tipo de pago no soportado: " + tipo);
        }
        return estrategia;
    }
}
