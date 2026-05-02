package com.marketplace.solicitud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Parámetros del ciclo de suscripción del vendedor activo (vencimiento, mora y cancelación automática).
 */
@ConfigurationProperties(prefix = "solicitud.suscripcion")
public class SuscripcionProperties {

    /**
     * Periodo por defecto si la petición de activación/renovación no envía {@code periodoSuscripcion}.
     */
    private Duration periodoRenovacion = Duration.ofDays(30);

    /**
     * Tiempo tras la fecha de vencimiento antes de pasar a EN_MORA (caso estudio: entre 1 día y 1 mes; valor por defecto 1 día).
     */
    private Duration demoraAntesMora = Duration.ofDays(1);

    /**
     * Tiempo en EN_MORA sin renovar antes de CANCELADA (caso estudio: ~un mes).
     */
    private Duration graciaCancelacionDesdeMora = Duration.ofDays(30);

    /**
     * Si una fila quedó EN_MORA antes de existir {@code entradaEnMoraEn}, se usa vencimiento + esta gracia para cancelar (compatibilidad).
     */
    private Duration graciaCancelacionLegacySinFechaMora = Duration.ofDays(30);

    private boolean schedulerHabilitado = true;

    private long jobFixedDelayMs = 3_600_000L;

    public Duration getPeriodoRenovacion() {
        return periodoRenovacion;
    }

    public void setPeriodoRenovacion(Duration periodoRenovacion) {
        this.periodoRenovacion = periodoRenovacion;
    }

    public Duration getDemoraAntesMora() {
        return demoraAntesMora;
    }

    public void setDemoraAntesMora(Duration demoraAntesMora) {
        this.demoraAntesMora = demoraAntesMora;
    }

    public Duration getGraciaCancelacionDesdeMora() {
        return graciaCancelacionDesdeMora;
    }

    public void setGraciaCancelacionDesdeMora(Duration graciaCancelacionDesdeMora) {
        this.graciaCancelacionDesdeMora = graciaCancelacionDesdeMora;
    }

    public Duration getGraciaCancelacionLegacySinFechaMora() {
        return graciaCancelacionLegacySinFechaMora;
    }

    public void setGraciaCancelacionLegacySinFechaMora(Duration graciaCancelacionLegacySinFechaMora) {
        this.graciaCancelacionLegacySinFechaMora = graciaCancelacionLegacySinFechaMora;
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
