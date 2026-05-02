package com.marketplace.solicitud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Umbrales de reputación (caso estudio §6): cancelación automática si hay muchas calificaciones muy malas o promedio bajo.
 */
@ConfigurationProperties(prefix = "solicitud.reputacion")
public class ReputacionProperties {

    /** Cantidad mínima de calificaciones estrictamente inferiores a 3 para disparar cancelación. */
    private int umbralCalificacionesMalas = 10;

    /** Si el promedio es menor que este valor (con al menos una calificación), se cancela. */
    private double umbralPromedio = 5.0;

    private boolean schedulerHabilitado = true;

    private long jobFixedDelayMs = 3_600_000L;

    public int getUmbralCalificacionesMalas() {
        return umbralCalificacionesMalas;
    }

    public void setUmbralCalificacionesMalas(int umbralCalificacionesMalas) {
        this.umbralCalificacionesMalas = umbralCalificacionesMalas;
    }

    public double getUmbralPromedio() {
        return umbralPromedio;
    }

    public void setUmbralPromedio(double umbralPromedio) {
        this.umbralPromedio = umbralPromedio;
    }

    public boolean isSchedulerHabilitado() {
        return schedulerHabilitado;
    }

    public void setSchedulerHabilitado(boolean schedulerHabilitado) {
        this.schedulerHabilitado = schedulerHabilitado;
    }

    public long getJobFixedDelayMs() {
        return jobFixedDelayMs;
    }

    public void setJobFixedDelayMs(long jobFixedDelayMs) {
        this.jobFixedDelayMs = jobFixedDelayMs;
    }
}
