package com.marketplace.order.pricing;

import com.marketplace.order.domain.BorradorOrden;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Propósito: aplicar un porcentaje de impuesto sobre el monto devuelto por el calculador envuelto.
 * Patrón: Decorator (añade responsabilidad fiscal sin modificar el componente base).
 * Responsabilidad: delegar en {@link #delegado}, multiplicar por alícuota y redondear HALF_UP.
 */
public class ImpuestoVentaDecorador implements CalculadorPrecioOrden {

    private final CalculadorPrecioOrden delegado;
    private final BigDecimal alicuota;

    public ImpuestoVentaDecorador(CalculadorPrecioOrden delegado, BigDecimal alicuota) {
        this.delegado = delegado;
        this.alicuota = alicuota;
    }

    @Override
    public BigDecimal calcular(BorradorOrden borrador) {
        BigDecimal base = delegado.calcular(borrador);
        return base.multiply(BigDecimal.ONE.add(alicuota)).setScale(4, RoundingMode.HALF_UP);
    }
}
