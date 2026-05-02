package com.marketplace.order.pricing;

import com.marketplace.order.domain.BorradorOrden;
import com.marketplace.order.dto.DesglosePrecioOrden;
import com.marketplace.order.pricing.shipping.PoliticaCargoEnvio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calcula el desglose coherente con la cadena {@link CalculadorPrecioOrden} configurada (IVA multiplicativo sobre subtotal, comisión sobre subtotal base, envío por política).
 */
@Component
public class CalculadoraDesglosePrecioOrden {

    private final CalculadorPrecioOrden cadenaTotales;
    private final BigDecimal tasaIva;
    private final BigDecimal tasaComision;
    private final PoliticaCargoEnvio politicaCargoEnvio;

    public CalculadoraDesglosePrecioOrden(
            CalculadorPrecioOrden cadenaTotales,
            PoliticaCargoEnvio politicaCargoEnvio,
            @Value("${order.pricing.tax-rate:0.19}") BigDecimal tasaIva,
            @Value("${order.pricing.commission-rate:0.05}") BigDecimal tasaComision) {
        this.cadenaTotales = cadenaTotales;
        this.politicaCargoEnvio = politicaCargoEnvio;
        this.tasaIva = tasaIva;
        this.tasaComision = tasaComision;
    }

    public DesglosePrecioOrden desglose(BorradorOrden borrador) {
        BigDecimal subtotalBase = borrador.subtotalLineas().setScale(4, RoundingMode.HALF_UP);
        BigDecimal montoIva = subtotalBase.multiply(tasaIva).setScale(4, RoundingMode.HALF_UP);
        BigDecimal montoComision = subtotalBase.multiply(tasaComision).setScale(4, RoundingMode.HALF_UP);
        BigDecimal montoEnvio = politicaCargoEnvio.cargo(borrador).setScale(4, RoundingMode.HALF_UP);
        BigDecimal total = cadenaTotales.calcular(borrador).setScale(4, RoundingMode.HALF_UP);
        BigDecimal sumaPartes =
                subtotalBase.add(montoIva).add(montoComision).add(montoEnvio).setScale(4, RoundingMode.HALF_UP);
        if (sumaPartes.compareTo(total) != 0) {
            throw new IllegalStateException(
                    "Inconsistencia de totales: suma desglose=" + sumaPartes + " cadena decoradores=" + total);
        }
        return new DesglosePrecioOrden(subtotalBase, montoIva, montoComision, montoEnvio, total);
    }
}
