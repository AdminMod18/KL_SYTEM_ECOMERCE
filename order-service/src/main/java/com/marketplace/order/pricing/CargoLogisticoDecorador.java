package com.marketplace.order.pricing;

import com.marketplace.order.domain.BorradorOrden;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Propósito: sumar un cargo fijo de logística al total ya procesado por decoradores internos.
 * Patrón: Decorator (extiende el resultado con un fee transversal).
 * Responsabilidad: delegar cálculo previo y sumar el monto fijo configurado.
 */
public class CargoLogisticoDecorador implements CalculadorPrecioOrden {

    private final CalculadorPrecioOrden delegado;
    private final BigDecimal cargoFijo;

    public CargoLogisticoDecorador(CalculadorPrecioOrden delegado, BigDecimal cargoFijo) {
        this.delegado = delegado;
        this.cargoFijo = cargoFijo;
    }

    @Override
    public BigDecimal calcular(BorradorOrden borrador) {
        BigDecimal parcial = delegado.calcular(borrador);
        return parcial.add(cargoFijo).setScale(4, RoundingMode.HALF_UP);
    }
}
