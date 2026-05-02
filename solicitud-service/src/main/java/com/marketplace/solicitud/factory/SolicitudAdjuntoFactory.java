package com.marketplace.solicitud.factory;

import com.marketplace.solicitud.dto.AdjuntoCreateDto;
import com.marketplace.solicitud.entity.Solicitud;
import com.marketplace.solicitud.entity.SolicitudAdjunto;

/**
 * Propósito: crear filas de adjunto a partir del DTO de entrada y la solicitud padre.
 * Patrón: Factory Method (objeto producto {@link SolicitudAdjunto} encapsulado).
 * Responsabilidad: mantener la construcción de entidades fuera del servicio de aplicación.
 */
public final class SolicitudAdjuntoFactory {

    private SolicitudAdjuntoFactory() {}

    public static SolicitudAdjunto crear(Solicitud solicitud, AdjuntoCreateDto dto) {
        SolicitudAdjunto adj = new SolicitudAdjunto();
        adj.setSolicitud(solicitud);
        adj.setTipo(dto.getTipo());
        adj.setNombreArchivo(dto.getNombreArchivo().trim());
        return adj;
    }
}
