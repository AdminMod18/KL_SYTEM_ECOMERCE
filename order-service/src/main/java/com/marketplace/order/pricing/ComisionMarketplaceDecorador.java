package com.marketplace.order.pricing;

import com.marketplace.order.domain.BorradorOrden;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Comisión del marketplace como porcentaje del subtotal de líneas (precio base), aplicada después del IVA.
 */
public class ComisionMarketplaceDecorador implements CalculadorPrecioOrden {

    private final CalculadorPrecioOrden delegado;
    private final BigDecimal tasaComision;

    public ComisionMarketplaceDecorador(CalculadorPrecioOrden delegado, BigDecimal tasaComision) {
        this.delegado = delegado;
        this.tasaComision = tasaComision;
    }

    @Override
    public BigDecimal calcular(BorradorOrden borrador) {
        BigDecimal subtotal = borrador.subtotalLineas();
        BigDecimal comision = subtotal.multiply(tasaComision).setScale(4, RoundingMode.HALF_UP);
        return delegado.calcular(borrador).add(comision).setScale(4, RoundingMode.HALF_UP);
    }
}
