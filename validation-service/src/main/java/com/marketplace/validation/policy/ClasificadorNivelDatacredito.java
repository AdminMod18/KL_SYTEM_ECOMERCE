package com.marketplace.validation.policy;

import com.marketplace.validation.domain.NivelClasificacionProveedor;
import com.marketplace.validation.domain.ResultadoDatacredito;

/**
 * Traduce el resultado técnico de Datacrédito al nivel de negocio ALTA / BAJA / ADVERTENCIA.
 */
public final class ClasificadorNivelDatacredito {

    private ClasificadorNivelDatacredito() {
    }

    public static NivelClasificacionProveedor clasificar(ResultadoDatacredito r) {
        if (r.listaControl() || r.score() < UmbralScoresPoliticaVendedor.RECHAZO_EXCLUSIVO_HASTA) {
            return NivelClasificacionProveedor.BAJA;
        }
        if (r.score() < UmbralScoresPoliticaVendedor.ADVERTENCIA_EXCLUSIVA_HASTA) {
            return NivelClasificacionProveedor.ADVERTENCIA;
        }
        return NivelClasificacionProveedor.ALTA;
    }
}
