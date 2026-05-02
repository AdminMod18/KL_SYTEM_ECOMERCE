package com.marketplace.solicitud.config;

import java.time.Duration;

/**
 * Valores efectivos de mora, suscripción y reputación tras aplicar YAML y, opcionalmente, overrides desde admin-service.
 */
public record ParamSnapshot(
        int umbralCalificacionesMalas,
        double umbralPromedio,
        Duration demoraAntesMora,
        Duration graciaCancelacionDesdeMora,
        Duration graciaCancelacionLegacySinFechaMora) {

    static ParamSnapshot desde(ReputacionProperties reputacion, SuscripcionProperties suscripcion) {
        return new ParamSnapshot(
                reputacion.getUmbralCalificacionesMalas(),
                reputacion.getUmbralPromedio(),
                suscripcion.getDemoraAntesMora(),
                suscripcion.getGraciaCancelacionDesdeMora(),
                suscripcion.getGraciaCancelacionLegacySinFechaMora());
    }
}
