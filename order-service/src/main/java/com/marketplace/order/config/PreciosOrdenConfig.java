package com.marketplace.order.config;

import com.marketplace.order.pricing.CalculadorPrecioOrden;
import com.marketplace.order.pricing.CargoLogisticoDecorador;
import com.marketplace.order.pricing.ComisionMarketplaceDecorador;
import com.marketplace.order.pricing.ImpuestoVentaDecorador;
import com.marketplace.order.pricing.SubtotalLineasCalculador;
import com.marketplace.order.pricing.shipping.PoliticaCargoEnvio;
import com.marketplace.order.pricing.shipping.PoliticaCargoEnvioPorRegion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Propósito: ensamblar la cadena decorada de cálculo de totales para órdenes.
 * Patrón: Object mother / Composition Root (ensambla Decorator sobre ConcreteComponent).
 * Responsabilidad: exponer un único {@link CalculadorPrecioOrden} con subtotal → impuesto → comisión → envío.
 */
@Configuration
public class PreciosOrdenConfig {

    @Bean
    public PoliticaCargoEnvio politicaCargoEnvio(
            @Value("${order.pricing.shipping-flat:5.00}") BigDecimal shippingLocal,
            @Value("${order.pricing.shipping-nacional:8.00}") BigDecimal shippingNacional,
            @Value("${order.pricing.shipping-internacional:25.00}") BigDecimal shippingInternacional) {
        return new PoliticaCargoEnvioPorRegion(shippingLocal, shippingNacional, shippingInternacional);
    }

    @Bean
    public CalculadorPrecioOrden calculadorPrecioOrden(
            PoliticaCargoEnvio politicaCargoEnvio,
            @Value("${order.pricing.tax-rate:0.19}") BigDecimal taxRate,
            @Value("${order.pricing.commission-rate:0.05}") BigDecimal commissionRate) {
        CalculadorPrecioOrden subtotal = new SubtotalLineasCalculador();
        CalculadorPrecioOrden conImpuesto = new ImpuestoVentaDecorador(subtotal, taxRate);
        CalculadorPrecioOrden conComision = new ComisionMarketplaceDecorador(conImpuesto, commissionRate);
        return new CargoLogisticoDecorador(conComision, politicaCargoEnvio::cargo);
    }
}
