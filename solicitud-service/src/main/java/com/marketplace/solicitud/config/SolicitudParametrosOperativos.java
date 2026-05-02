package com.marketplace.solicitud.config;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Snapshot thread-safe de parámetros que usan los jobs de suscripción y reputación.
 * Parte de {@link ReputacionProperties} / {@link SuscripcionProperties}; puede sobreescribirse al sincronizar con admin-service.
 */
@Component
public class SolicitudParametrosOperativos {

    public static final String CLAVE_REPUTACION_MALAS = "solicitud.reputacion.umbral-calificaciones-malas";
    public static final String CLAVE_REPUTACION_PROMEDIO = "solicitud.reputacion.umbral-promedio";
    public static final String CLAVE_DEMORA_MORA = "solicitud.suscripcion.demora-antes-mora";
    public static final String CLAVE_GRACIA_MORA = "solicitud.suscripcion.gracia-cancelacion-desde-mora";

    private final ReputacionProperties reputacionProperties;
    private final SuscripcionProperties suscripcionProperties;

    private final AtomicReference<ParamSnapshot> snapshot = new AtomicReference<>();

    public SolicitudParametrosOperativos(
            ReputacionProperties reputacionProperties, SuscripcionProperties suscripcionProperties) {
        this.reputacionProperties = reputacionProperties;
        this.suscripcionProperties = suscripcionProperties;
    }

    @PostConstruct
    void iniciar() {
        reinicializarDesdePropiedades();
    }

    /** Repone valores desde YAML (útil en tests o tras cambiar perfil). */
    public void reinicializarDesdePropiedades() {
        snapshot.set(ParamSnapshot.desde(reputacionProperties, suscripcionProperties));
    }

    public ParamSnapshot actual() {
        return snapshot.get();
    }

    /**
     * Aplica solo claves conocidas presentes en el mapa; el resto se mantiene del snapshot actual.
     */
    public void aplicarValoresAdminSiPresentes(Map<String, String> porClave) {
        if (porClave == null || porClave.isEmpty()) {
            return;
        }
        ParamSnapshot cur = snapshot.get();
        int malas = parseInt(porClave.get(CLAVE_REPUTACION_MALAS), cur.umbralCalificacionesMalas());
        double prom = parseDouble(porClave.get(CLAVE_REPUTACION_PROMEDIO), cur.umbralPromedio());
        Duration demora = parseDuration(porClave.get(CLAVE_DEMORA_MORA), cur.demoraAntesMora());
        Duration gracia = parseDuration(porClave.get(CLAVE_GRACIA_MORA), cur.graciaCancelacionDesdeMora());
        snapshot.set(
                new ParamSnapshot(
                        malas,
                        prom,
                        demora,
                        gracia,
                        cur.graciaCancelacionLegacySinFechaMora()));
    }

    private static int parseInt(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static double parseDouble(String raw, double fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Double.parseDouble(raw.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static Duration parseDuration(String raw, Duration fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Duration.parse(raw.trim());
        } catch (Exception ex) {
            return fallback;
        }
    }
}
