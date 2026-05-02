package com.marketplace.solicitud.integration;

/**
 * Invoca el servicio de validación externa y devuelve el estado sugerido para la solicitud.
 *
 * @param exigenciaJudicialOverride {@code REQUERIDO}, {@code NO_REQUERIDO} o {@code null} (mock por documento).
 */
public interface ValidacionVendedorGateway {

    ValidacionVendedorResult ejecutarValidacion(
            String documentoIdentidad,
            String nombreVendedor,
            String contenidoArchivoCifin,
            String exigenciaJudicialOverride);
}
