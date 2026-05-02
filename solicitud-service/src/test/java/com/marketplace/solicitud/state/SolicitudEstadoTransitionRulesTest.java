package com.marketplace.solicitud.state;

import com.marketplace.solicitud.exception.SolicitudBusinessException;
import com.marketplace.solicitud.model.SolicitudEstado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Reglas de transición del patrón State para PENDIENTE, APROBADA, RECHAZADA, DEVUELTA (+ estados posteriores del ciclo de vida).
 */
@SpringBootTest
class SolicitudEstadoTransitionRulesTest {

    @Autowired
    private SolicitudEstadoRegistry registry;

    @Test
    @DisplayName("PENDIENTE: solo APROBADA, RECHAZADA o DEVUELTA (no saltos a ACTIVA/CANCELADA)")
    void pendiente_transicionesPermitidasYbloqueos() {
        var s = registry.comportamientoPara(SolicitudEstado.PENDIENTE);
        assertThat(s.permiteValidacionAutomatica()).isTrue();
        assertThatCode(() -> s.assertTransicionPermitida(SolicitudEstado.APROBADA)).doesNotThrowAnyException();
        assertThatCode(() -> s.assertTransicionPermitida(SolicitudEstado.RECHAZADA)).doesNotThrowAnyException();
        assertThatCode(() -> s.assertTransicionPermitida(SolicitudEstado.DEVUELTA)).doesNotThrowAnyException();

        assertThatThrownBy(() -> s.assertTransicionPermitida(SolicitudEstado.ACTIVA))
                .isInstanceOf(SolicitudBusinessException.class)
                .satisfies(ex -> assertThat(((SolicitudBusinessException) ex).getCodigo()).isEqualTo("TRANSICION_INVALIDA"));
        assertThatThrownBy(() -> s.assertTransicionPermitida(SolicitudEstado.CANCELADA))
                .isInstanceOf(SolicitudBusinessException.class);
        assertThatCode(() -> s.assertTransicionPermitida(SolicitudEstado.PENDIENTE))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("DEVUELTA: revalidación automática (APROBADA/RECHAZADA/DEVUELTA), PENDIENTE o CANCELADA")
    void devuelta_permiteRevalidacion() {
        var s = registry.comportamientoPara(SolicitudEstado.DEVUELTA);
        assertThat(s.permiteValidacionAutomatica()).isTrue();
        assertThatCode(() -> s.assertTransicionPermitida(SolicitudEstado.PENDIENTE)).doesNotThrowAnyException();
        assertThatCode(() -> s.assertTransicionPermitida(SolicitudEstado.CANCELADA)).doesNotThrowAnyException();
        assertThatCode(() -> s.assertTransicionPermitida(SolicitudEstado.APROBADA)).doesNotThrowAnyException();
        assertThatCode(() -> s.assertTransicionPermitida(SolicitudEstado.RECHAZADA)).doesNotThrowAnyException();
        assertThatCode(() -> s.assertTransicionPermitida(SolicitudEstado.DEVUELTA)).doesNotThrowAnyException();

        assertThatThrownBy(() -> s.assertTransicionPermitida(SolicitudEstado.ACTIVA))
                .isInstanceOf(SolicitudBusinessException.class);
    }

    @Test
    @DisplayName("APROBADA: solo ACTIVA o CANCELADA (no regreso a PENDIENTE)")
    void aprobada_noRegresaPendiente() {
        var s = registry.comportamientoPara(SolicitudEstado.APROBADA);
        assertThat(s.permiteValidacionAutomatica()).isFalse();
        assertThatCode(() -> s.assertTransicionPermitida(SolicitudEstado.ACTIVA)).doesNotThrowAnyException();
        assertThatCode(() -> s.assertTransicionPermitida(SolicitudEstado.CANCELADA)).doesNotThrowAnyException();

        assertThatThrownBy(() -> s.assertTransicionPermitida(SolicitudEstado.PENDIENTE))
                .isInstanceOf(SolicitudBusinessException.class);
        assertThatThrownBy(() -> s.assertTransicionPermitida(SolicitudEstado.DEVUELTA))
                .isInstanceOf(SolicitudBusinessException.class);
    }

    @Test
    @DisplayName("ACTIVA: EN_MORA o CANCELADA")
    void activa_moraOCancelacion() {
        var s = registry.comportamientoPara(SolicitudEstado.ACTIVA);
        assertThatCode(() -> s.assertTransicionPermitida(SolicitudEstado.EN_MORA)).doesNotThrowAnyException();
        assertThatCode(() -> s.assertTransicionPermitida(SolicitudEstado.CANCELADA)).doesNotThrowAnyException();
        assertThatThrownBy(() -> s.assertTransicionPermitida(SolicitudEstado.PENDIENTE))
                .isInstanceOf(SolicitudBusinessException.class);
    }

    @Test
    @DisplayName("EN_MORA: regularización a ACTIVA o cierre CANCELADA")
    void enMora_regularizaOCierra() {
        var s = registry.comportamientoPara(SolicitudEstado.EN_MORA);
        assertThatCode(() -> s.assertTransicionPermitida(SolicitudEstado.ACTIVA)).doesNotThrowAnyException();
        assertThatCode(() -> s.assertTransicionPermitida(SolicitudEstado.CANCELADA)).doesNotThrowAnyException();
        assertThatThrownBy(() -> s.assertTransicionPermitida(SolicitudEstado.PENDIENTE))
                .isInstanceOf(SolicitudBusinessException.class);
    }

    @Test
    @DisplayName("RECHAZADA: solo CANCELADA (no reabrir a PENDIENTE)")
    void rechazada_soloCancelada() {
        var s = registry.comportamientoPara(SolicitudEstado.RECHAZADA);
        assertThat(s.permiteValidacionAutomatica()).isFalse();
        assertThatCode(() -> s.assertTransicionPermitida(SolicitudEstado.CANCELADA)).doesNotThrowAnyException();

        assertThatThrownBy(() -> s.assertTransicionPermitida(SolicitudEstado.PENDIENTE))
                .isInstanceOf(SolicitudBusinessException.class);
        assertThatThrownBy(() -> s.assertTransicionPermitida(SolicitudEstado.APROBADA))
                .isInstanceOf(SolicitudBusinessException.class);
    }

    @Test
    @DisplayName("Cada estado expone descripción de flujo no vacía")
    void descripcionesDefinidas() {
        for (SolicitudEstado e : SolicitudEstado.values()) {
            String d = registry.comportamientoPara(e).descripcionFlujo();
            assertThat(d).isNotBlank();
        }
    }
}
