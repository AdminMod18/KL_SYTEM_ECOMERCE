package com.marketplace.validation;

import com.marketplace.validation.domain.EstadoResultadoValidacionVendedor;
import com.marketplace.validation.domain.ExigenciaJudicial;
import com.marketplace.validation.domain.NivelClasificacionProveedor;
import com.marketplace.validation.policy.PoliticaEstadoVendedor;
import org.junit.jupiter.api.Test;

/**
 * Salida por consola (./gradlew test --tests FlujoVendedorValidacionQaConsoleTest) para revisión QA.
 */
class FlujoVendedorValidacionQaConsoleTest {

    @Test
    void imprimir_escenarios_politica() {
        System.out.println();
        System.out.println("========== QA — Política solicitud vendedor ==========");

        logCaso(
                "1) BAJA Datacrédito",
                NivelClasificacionProveedor.BAJA,
                NivelClasificacionProveedor.ALTA,
                ExigenciaJudicial.NO_REQUERIDO);

        logCaso(
                "2) BAJA CIFIN",
                NivelClasificacionProveedor.ALTA,
                NivelClasificacionProveedor.BAJA,
                ExigenciaJudicial.NO_REQUERIDO);

        logCaso(
                "3) ADVERTENCIA (sin BAJA)",
                NivelClasificacionProveedor.ADVERTENCIA,
                NivelClasificacionProveedor.ALTA,
                ExigenciaJudicial.NO_REQUERIDO);

        logCaso(
                "4) ALTA + ALTA + judicial NO_REQUERIDO",
                NivelClasificacionProveedor.ALTA,
                NivelClasificacionProveedor.ALTA,
                ExigenciaJudicial.NO_REQUERIDO);

        logCaso(
                "5) ALTA + ALTA + judicial REQUERIDO",
                NivelClasificacionProveedor.ALTA,
                NivelClasificacionProveedor.ALTA,
                ExigenciaJudicial.REQUERIDO);

        System.out.println("======================================================");
        System.out.println();
    }

    private static void logCaso(
            String titulo,
            NivelClasificacionProveedor dc,
            NivelClasificacionProveedor cifin,
            ExigenciaJudicial judicial) {

        var politica = PoliticaEstadoVendedor.evaluar(dc, cifin, judicial);
        EstadoResultadoValidacionVendedor estado = politica.estado();

        System.out.println();
        System.out.println("--- " + titulo + " ---");
        System.out.println("Solicitud creada (simulada): estado inicial = PENDIENTE");
        System.out.println("Resultado validación: DC=" + dc + ", CIFIN=" + cifin + ", judicial=" + judicial);
        System.out.println("Estado final sugerido: " + estado);
        System.out.println("Observaciones: " + politica.observacionesPolitica());
    }
}
