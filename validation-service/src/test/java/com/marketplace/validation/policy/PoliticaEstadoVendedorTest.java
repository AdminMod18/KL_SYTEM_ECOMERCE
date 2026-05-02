package com.marketplace.validation.policy;

import com.marketplace.validation.domain.EstadoResultadoValidacionVendedor;
import com.marketplace.validation.domain.ExigenciaJudicial;
import com.marketplace.validation.domain.NivelClasificacionProveedor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PoliticaEstadoVendedorTest {

    @Test
    void baja_datacredito_rechaza() {
        var r = PoliticaEstadoVendedor.evaluar(
                NivelClasificacionProveedor.BAJA,
                NivelClasificacionProveedor.ALTA,
                ExigenciaJudicial.NO_REQUERIDO);
        assertThat(r.estado()).isEqualTo(EstadoResultadoValidacionVendedor.RECHAZADA);
    }

    @Test
    void baja_cifin_rechaza() {
        var r = PoliticaEstadoVendedor.evaluar(
                NivelClasificacionProveedor.ALTA,
                NivelClasificacionProveedor.BAJA,
                ExigenciaJudicial.NO_REQUERIDO);
        assertThat(r.estado()).isEqualTo(EstadoResultadoValidacionVendedor.RECHAZADA);
    }

    @Test
    void advertencia_sin_baja_devuelve() {
        var r = PoliticaEstadoVendedor.evaluar(
                NivelClasificacionProveedor.ADVERTENCIA,
                NivelClasificacionProveedor.ALTA,
                ExigenciaJudicial.NO_REQUERIDO);
        assertThat(r.estado()).isEqualTo(EstadoResultadoValidacionVendedor.DEVUELTA);
    }

    @Test
    void ambas_alta_y_judicial_no_requerido_aprueba() {
        var r = PoliticaEstadoVendedor.evaluar(
                NivelClasificacionProveedor.ALTA,
                NivelClasificacionProveedor.ALTA,
                ExigenciaJudicial.NO_REQUERIDO);
        assertThat(r.estado()).isEqualTo(EstadoResultadoValidacionVendedor.APROBADA);
    }

    @Test
    void ambas_alta_pero_judicial_requerido_devuelve() {
        var r = PoliticaEstadoVendedor.evaluar(
                NivelClasificacionProveedor.ALTA,
                NivelClasificacionProveedor.ALTA,
                ExigenciaJudicial.REQUERIDO);
        assertThat(r.estado()).isEqualTo(EstadoResultadoValidacionVendedor.DEVUELTA);
    }

    @Test
    void baja_prevalece_sobre_advertencia_en_otro() {
        var r = PoliticaEstadoVendedor.evaluar(
                NivelClasificacionProveedor.BAJA,
                NivelClasificacionProveedor.ADVERTENCIA,
                ExigenciaJudicial.NO_REQUERIDO);
        assertThat(r.estado()).isEqualTo(EstadoResultadoValidacionVendedor.RECHAZADA);
    }
}
