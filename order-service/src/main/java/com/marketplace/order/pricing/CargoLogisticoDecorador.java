package com.marketplace.order.pricing;

import com.marketplace.order.domain.BorradorOrden;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

/**
 * Propósito: sumar el cargo de logística al total ya procesado por decoradores internos.
 * Patrón: Decorator (extiende el resultado con un fee transversal).
 * Responsabilidad: delegar cálculo previo y sumar el monto según {@link Function} (p. ej. política por ciudad/país).
 */
public class CargoLogisticoDecorador implements CalculadorPrecioOrden {

    private final CalculadorPrecioOrden delegado;
    private final Function<BorradorOrden, BigDecimal> resolverCargo;

    public CargoLogisticoDecorador(CalculadorPrecioOrden delegado, Function<BorradorOrden, BigDecimal> resolverCargo) {
        this.delegado = delegado;
        this.resolverCargo = resolverCargo;
    }

    @Override
    public BigDecimal calcular(BorradorOrden borrador) {
        BigDecimal parcial = delegado.calcular(borrador);
        BigDecimal cargo = resolverCargo.apply(borrador);
        return parcial.add(cargo).setScale(4, RoundingMode.HALF_UP);
    }
}
