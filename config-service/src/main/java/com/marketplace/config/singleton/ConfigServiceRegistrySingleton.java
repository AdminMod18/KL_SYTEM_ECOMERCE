package com.marketplace.config.singleton;

import java.time.Instant;

/**
 * Propósito: mantener metadatos globales únicos del proceso del config-service (identidad y arranque).
 * Patrón: Singleton (variante idiomática recomendada en Java: enum con una sola constante {@code INSTANCE}).
 * Responsabilidad: garantizar una única instancia accesible vía {@link #INSTANCE} sin estado mutable compartido inseguro.
 */
public enum ConfigServiceRegistrySingleton {

    INSTANCE;

    private final Instant iniciadoEn = Instant.now();
    private final String nombreServicio = "config-service";
    private final String version = "1.0.0";

    public Instant getIniciadoEn() {
        return iniciadoEn;
    }

    public String getNombreServicio() {
        return nombreServicio;
    }

    public String getVersion() {
        return version;
    }
}
