package com.marketplace.solicitud.model;

import java.time.Duration;

/**
 * Plan de suscripción del vendedor según caso de estudio (mensual, semestral o anual).
 */
public enum PeriodoSuscripcionPlan {
    MENSUAL(Duration.ofDays(30)),
    SEMESTRAL(Duration.ofDays(182)),
    ANUAL(Duration.ofDays(365));

    private final Duration duracion;

    PeriodoSuscripcionPlan(Duration duracion) {
        this.duracion = duracion;
    }

    public Duration getDuracion() {
        return duracion;
    }
}
