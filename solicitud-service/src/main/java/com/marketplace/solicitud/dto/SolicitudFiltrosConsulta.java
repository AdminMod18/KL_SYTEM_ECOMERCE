package com.marketplace.solicitud.dto;

import com.marketplace.solicitud.model.SolicitudEstado;

import java.time.Instant;

/**
 * Propósito: criterios opcionales para el listado de solicitudes (capa de aplicación).
 * Patrón: Parameter Object (reduce lista larga de argumentos).
 * Responsabilidad: transportar filtros desde HTTP hasta {@link com.marketplace.solicitud.facade.ConsultaSolicitudesDirectorFacade}.
 */
public record SolicitudFiltrosConsulta(
        Long solicitudIdExacta,
        String documentoIdentidadContiene,
        SolicitudEstado estado,
        Instant creadoDesdeInclusive,
        Instant creadoHastaInclusive,
        String textoLibreNombreCorreo) {

    public static SolicitudFiltrosConsulta sinFiltros() {
        return new SolicitudFiltrosConsulta(null, null, null, null, null, null);
    }
}
