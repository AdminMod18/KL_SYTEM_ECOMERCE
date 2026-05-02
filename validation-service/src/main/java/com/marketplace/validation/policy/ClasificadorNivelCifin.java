package com.marketplace.validation.policy;

import com.marketplace.validation.domain.NivelClasificacionProveedor;
import com.marketplace.validation.domain.ResultadoCifin;

/**
 * Traduce la línea CIFIN interpretada al nivel de negocio ALTA / BAJA / ADVERTENCIA,
 * usando los mismos cortes numéricos que Datacrédito ({@link UmbralScoresPoliticaVendedor})
 * sobre el indicador de riesgo de la línea, además de estados contractuales graves.
 */
public final class ClasificadorNivelCifin {

    private ClasificadorNivelCifin() {
    }

    public static NivelClasificacionProveedor clasificar(ResultadoCifin r) {
        if (!r.informacionEncontrada()) {
            return NivelClasificacionProveedor.BAJA;
        }
        String estadoUp = r.estadoLinea() == null ? "" : r.estadoLinea().trim().toUpperCase();
        if (estadoUp.equals("MORA") || estadoUp.equals("INCOBRABLE") || estadoUp.equals("CASTIGADO")) {
            return NivelClasificacionProveedor.BAJA;
        }
        if (estadoUp.equals("OBSERVADO") || estadoUp.equals("ALERTA") || estadoUp.equals("REVISAR")) {
            return NivelClasificacionProveedor.ADVERTENCIA;
        }

        int scoreLinea = r.indicadorRiesgo();
        if (scoreLinea < UmbralScoresPoliticaVendedor.RECHAZO_EXCLUSIVO_HASTA) {
            return NivelClasificacionProveedor.BAJA;
        }
        if (scoreLinea < UmbralScoresPoliticaVendedor.ADVERTENCIA_EXCLUSIVA_HASTA) {
            return NivelClasificacionProveedor.ADVERTENCIA;
        }
        if (estadoUp.equals("NORMAL")) {
            return NivelClasificacionProveedor.ALTA;
        }
        return NivelClasificacionProveedor.ADVERTENCIA;
    }
}
