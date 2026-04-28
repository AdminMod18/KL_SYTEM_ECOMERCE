package com.marketplace.order.config;

import com.marketplace.order.pricing.CalculadorPrecioOrden;
import com.marketplace.order.pricing.CargoLogisticoDecorador;
import com.marketplace.order.pricing.ImpuestoVentaDecorador;
import com.marketplace.order.pricing.SubtotalLineasCalculador;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Propósito: ensamblar la cadena decorada de cálculo de totales para órdenes.
 * Patrón: Object mother / Composition Root (ensambla Decorator sobre ConcreteComponent).
 * Responsabilidad: exponer un único {@link CalculadorPrecioOrden} con subtotal → impuesto → envío.
 */
@Configuration
public class PreciosOrdenConfig {

    @Bean
    public CalculadorPrecioOrden calculadorPrecioOrden(
            @Value("${order.pricing.tax-rate:0.19}") BigDecimal taxRate,
            @Value("${order.pricing.shipping-flat:5.00}") BigDecimal shippingFlat) {
        CalculadorPrecioOrden subtotal = new SubtotalLineasCalculador();
        CalculadorPrecioOrden conImpuesto = new ImpuestoVentaDecorador(subtotal, taxRate);
        return new CargoLogisticoDecorador(conImpuesto, shippingFlat);
    }
}
