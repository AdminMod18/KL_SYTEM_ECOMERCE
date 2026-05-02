package com.marketplace.order.pricing.shipping;

import com.marketplace.order.domain.BorradorOrden;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Propósito: tarifas escalonadas — Bogotá/local, otras ciudades CO, internacional.
 * Patrón: Strategy concreta (reglas explícitas y configurables por constructor).
 */
public class PoliticaCargoEnvioPorRegion implements PoliticaCargoEnvio {

    private final BigDecimal montoLocalOCapitulo;
    private final BigDecimal montoNacional;
    private final BigDecimal montoInternacional;

    public PoliticaCargoEnvioPorRegion(
            BigDecimal montoLocalOCapitulo, BigDecimal montoNacional, BigDecimal montoInternacional) {
        this.montoLocalOCapitulo = montoLocalOCapitulo;
        this.montoNacional = montoNacional;
        this.montoInternacional = montoInternacional;
    }

    @Override
    public BigDecimal cargo(BorradorOrden borrador) {
        String pais = borrador.paisEnvio();
        String ciudad = borrador.ciudadEnvio();
        if (pais == null || pais.isBlank()) {
            return montoLocalOCapitulo;
        }
        String p = normalizar(pais);
        if (!esColombia(p)) {
            return montoInternacional;
        }
        if (ciudad == null || ciudad.isBlank()) {
            return montoNacional;
        }
        String c = normalizar(ciudad);
        if (c.contains("bogota")) {
            return montoLocalOCapitulo;
        }
        return montoNacional;
    }

    private static boolean esColombia(String pNormalizado) {
        return pNormalizado.contains("colombia") || pNormalizado.equals("co");
    }

    private static String normalizar(String texto) {
        String sinTildes =
                Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return sinTildes.toLowerCase(Locale.ROOT);
    }
}
