package com.marketplace.validation.policy;

import com.marketplace.validation.domain.EstadoResultadoValidacionVendedor;
import com.marketplace.validation.domain.ExigenciaJudicial;
import com.marketplace.validation.domain.NivelClasificacionProveedor;

import java.util.ArrayList;
import java.util.List;

/**
 * Reglas de negocio agregadas para solicitud de vendedor.
 * Los scores numéricos (Datacrédito y campo de riesgo en línea CIFIN) usan
 * {@link UmbralScoresPoliticaVendedor}: &lt;550 BAJA, 550–649 ADVERTENCIA, ≥650 ALTA.
 * <ul>
 *   <li>Si Datacrédito o CIFIN = BAJA → RECHAZADA</li>
 *   <li>Si alguno = ADVERTENCIA → DEVUELTA</li>
 *   <li>Si ambos = ALTA y judicial = NO_REQUERIDO → APROBADA</li>
 *   <li>En otro caso (p. ej. judicial REQUERIDO con riesgos altos) → DEVUELTA</li>
 * </ul>
 */
public final class PoliticaEstadoVendedor {

    private PoliticaEstadoVendedor() {
    }

    public static ResultadoPolitica evaluar(
            NivelClasificacionProveedor datacredito,
            NivelClasificacionProveedor cifin,
            ExigenciaJudicial judicial) {

        List<String> observaciones = new ArrayList<>();

        if (datacredito == NivelClasificacionProveedor.BAJA) {
            observaciones.add("Datacrédito en nivel BAJA.");
        }
        if (cifin == NivelClasificacionProveedor.BAJA) {
            observaciones.add("CIFIN en nivel BAJA.");
        }
        if (datacredito == NivelClasificacionProveedor.ADVERTENCIA) {
            observaciones.add("Datacrédito en nivel ADVERTENCIA.");
        }
        if (cifin == NivelClasificacionProveedor.ADVERTENCIA) {
            observaciones.add("CIFIN en nivel ADVERTENCIA.");
        }
        if (judicial == ExigenciaJudicial.REQUERIDO) {
            observaciones.add("Validación judicial REQUERIDA.");
        }

        if (datacredito == NivelClasificacionProveedor.BAJA || cifin == NivelClasificacionProveedor.BAJA) {
            return new ResultadoPolitica(EstadoResultadoValidacionVendedor.RECHAZADA, observaciones);
        }
        if (datacredito == NivelClasificacionProveedor.ADVERTENCIA || cifin == NivelClasificacionProveedor.ADVERTENCIA) {
            return new ResultadoPolitica(EstadoResultadoValidacionVendedor.DEVUELTA, observaciones);
        }
        if (datacredito == NivelClasificacionProveedor.ALTA
                && cifin == NivelClasificacionProveedor.ALTA
                && judicial == ExigenciaJudicial.NO_REQUERIDO) {
            observaciones.add("Criterios automáticos cumplidos: aprobación sugerida.");
            return new ResultadoPolitica(EstadoResultadoValidacionVendedor.APROBADA, observaciones);
        }
        return new ResultadoPolitica(EstadoResultadoValidacionVendedor.DEVUELTA, observaciones);
    }

    public record ResultadoPolitica(EstadoResultadoValidacionVendedor estado, List<String> observacionesPolitica) {
    }
}
