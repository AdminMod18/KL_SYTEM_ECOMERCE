package com.marketplace.solicitud.service;

import com.marketplace.solicitud.dto.CalificacionVendedorCreateRequest;
import com.marketplace.solicitud.dto.CalificacionVendedorResponse;
import com.marketplace.solicitud.dto.ReputacionResumenResponse;
import com.marketplace.solicitud.entity.CalificacionVendedor;
import com.marketplace.solicitud.entity.Solicitud;
import com.marketplace.solicitud.exception.SolicitudBusinessException;
import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.repository.CalificacionVendedorRepository;
import com.marketplace.solicitud.repository.SolicitudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * Registro de calificaciones solo para vendedores en ciclo operativo (ACTIVA / EN_MORA).
 */
@Service
public class CalificacionVendedorService {

    private final SolicitudRepository solicitudRepository;
    private final CalificacionVendedorRepository calificacionVendedorRepository;

    public CalificacionVendedorService(
            SolicitudRepository solicitudRepository, CalificacionVendedorRepository calificacionVendedorRepository) {
        this.solicitudRepository = solicitudRepository;
        this.calificacionVendedorRepository = calificacionVendedorRepository;
    }

    /**
     * Catálogo público (HU-17): si el {@code vendedorSolicitudId} del producto no existe en esta BD (datos huérfanos u otro entorno),
     * se devuelve resumen vacío en lugar de 404 para no romper la ficha del producto.
     */
    @Transactional(readOnly = true)
    public ReputacionResumenResponse resumenPorSolicitud(Long solicitudId) {
        if (!solicitudRepository.existsById(solicitudId)) {
            return new ReputacionResumenResponse(solicitudId, 0L, null);
        }
        long total = calificacionVendedorRepository.countBySolicitud_Id(solicitudId);
        Optional<Double> prom = calificacionVendedorRepository.promedioValorPorSolicitud(solicitudId);
        return new ReputacionResumenResponse(solicitudId, total, prom.orElse(null));
    }

    @Transactional
    public CalificacionVendedorResponse registrar(Long solicitudId, CalificacionVendedorCreateRequest request) {
        Solicitud solicitud = solicitudRepository
                .findById(solicitudId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
        SolicitudEstado estado = solicitud.getEstado();
        if (estado != SolicitudEstado.ACTIVA && estado != SolicitudEstado.EN_MORA) {
            throw new SolicitudBusinessException(
                    "CALIFICACION_ESTADO_INVALIDO",
                    "Solo se pueden registrar calificaciones con solicitud ACTIVA o EN_MORA. Estado actual: " + estado);
        }
        CalificacionVendedor c = new CalificacionVendedor();
        c.setSolicitud(solicitud);
        c.setValor(request.getValor());
        if (request.getComentario() != null) {
            c.setComentario(request.getComentario().trim());
        }
        if (request.getReferenciaOrden() != null) {
            c.setReferenciaOrden(request.getReferenciaOrden().trim());
        }
        CalificacionVendedor guardada = calificacionVendedorRepository.save(c);
        return aRespuesta(guardada);
    }

    private static CalificacionVendedorResponse aRespuesta(CalificacionVendedor c) {
        return new CalificacionVendedorResponse(
                c.getId(),
                c.getSolicitud().getId(),
                c.getValor(),
                c.getComentario(),
                c.getReferenciaOrden(),
                c.getCreadoEn());
    }
}
