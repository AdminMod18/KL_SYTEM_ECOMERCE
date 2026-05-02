package com.marketplace.solicitud.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SolicitudParametrosOperativosTest {

    private ReputacionProperties reputacionProperties;
    private SuscripcionProperties suscripcionProperties;
    private SolicitudParametrosOperativos operativos;

    @BeforeEach
    void setUp() {
        reputacionProperties = new ReputacionProperties();
        reputacionProperties.setUmbralCalificacionesMalas(10);
        reputacionProperties.setUmbralPromedio(5.0);
        suscripcionProperties = new SuscripcionProperties();
        suscripcionProperties.setDemoraAntesMora(Duration.ofDays(1));
        suscripcionProperties.setGraciaCancelacionDesdeMora(Duration.ofDays(30));
        suscripcionProperties.setGraciaCancelacionLegacySinFechaMora(Duration.ofDays(30));
        operativos = new SolicitudParametrosOperativos(reputacionProperties, suscripcionProperties);
        operativos.reinicializarDesdePropiedades();
    }

    @Test
    @DisplayName("Merge desde admin actualiza umbrales y duraciones conocidas")
    void aplicarAdmin_actualizaSnapshot() {
        operativos.aplicarValoresAdminSiPresentes(
                Map.of(
                        SolicitudParametrosOperativos.CLAVE_REPUTACION_MALAS,
                        "3",
                        SolicitudParametrosOperativos.CLAVE_REPUTACION_PROMEDIO,
                        "4.5",
                        SolicitudParametrosOperativos.CLAVE_DEMORA_MORA,
                        "P2D",
                        SolicitudParametrosOperativos.CLAVE_GRACIA_MORA,
                        "P10D"));

        ParamSnapshot s = operativos.actual();
        assertThat(s.umbralCalificacionesMalas()).isEqualTo(3);
        assertThat(s.umbralPromedio()).isEqualTo(4.5);
        assertThat(s.demoraAntesMora()).isEqualTo(Duration.ofDays(2));
        assertThat(s.graciaCancelacionDesdeMora()).isEqualTo(Duration.ofDays(10));
        assertThat(s.graciaCancelacionLegacySinFechaMora()).isEqualTo(Duration.ofDays(30));
    }
}
