package com.marketplace.validation.policy;

import com.marketplace.validation.domain.NivelClasificacionProveedor;
import com.marketplace.validation.domain.ResultadoCifin;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClasificadorNivelCifinTest {

    @Test
    void sin_linea_es_baja() {
        assertThat(ClasificadorNivelCifin.clasificar(new ResultadoCifin(0, "NO_ENCONTRADO", false)))
                .isEqualTo(NivelClasificacionProveedor.BAJA);
    }

    @Test
    void score_menor_550_es_baja_aunque_normal() {
        assertThat(ClasificadorNivelCifin.clasificar(new ResultadoCifin(0, "NORMAL", true)))
                .isEqualTo(NivelClasificacionProveedor.BAJA);
        assertThat(ClasificadorNivelCifin.clasificar(new ResultadoCifin(549, "NORMAL", true)))
                .isEqualTo(NivelClasificacionProveedor.BAJA);
    }

    @Test
    void score_550_a_649_es_advertencia_si_normal() {
        assertThat(ClasificadorNivelCifin.clasificar(new ResultadoCifin(550, "NORMAL", true)))
                .isEqualTo(NivelClasificacionProveedor.ADVERTENCIA);
        assertThat(ClasificadorNivelCifin.clasificar(new ResultadoCifin(649, "NORMAL", true)))
                .isEqualTo(NivelClasificacionProveedor.ADVERTENCIA);
    }

    @Test
    void score_650_o_mas_y_normal_es_alta() {
        assertThat(ClasificadorNivelCifin.clasificar(new ResultadoCifin(650, "NORMAL", true)))
                .isEqualTo(NivelClasificacionProveedor.ALTA);
        assertThat(ClasificadorNivelCifin.clasificar(new ResultadoCifin(900, "NORMAL", true)))
                .isEqualTo(NivelClasificacionProveedor.ALTA);
    }

    @Test
    void mora_prevalece_sobre_score_alto() {
        assertThat(ClasificadorNivelCifin.clasificar(new ResultadoCifin(900, "MORA", true)))
                .isEqualTo(NivelClasificacionProveedor.BAJA);
    }
}
