package com.marketplace.validation.policy;

/**
 * Umbrales numéricos únicos para Datacrédito y línea CIFIN (indicador de riesgo).
 * <ul>
 *   <li>{@code score &lt; RECHAZO_EXCLUSIVO_HASTA} → nivel {@link com.marketplace.validation.domain.NivelClasificacionProveedor#BAJA} → RECHAZADA</li>
 *   <li>{@code RECHAZO_EXCLUSIVO_HASTA ≤ score &lt; ADVERTENCIA_EXCLUSIVA_HASTA} → ADVERTENCIA → DEVUELTA</li>
 *   <li>{@code score ≥ ADVERTENCIA_EXCLUSIVA_HASTA} → ALTA (salvo reglas de estado de línea / judicial en política)</li>
 * </ul>
 */
public final class UmbralScoresPoliticaVendedor {

    /** Exclusivo superior del tramo rechazo: valores estrictamente menores son BAJA. */
    public static final int RECHAZO_EXCLUSIVO_HASTA = 550;

    /** Exclusivo superior del tramo advertencia: desde 550 hasta 649 es ADVERTENCIA. */
    public static final int ADVERTENCIA_EXCLUSIVA_HASTA = 650;

    private UmbralScoresPoliticaVendedor() {
    }
}
