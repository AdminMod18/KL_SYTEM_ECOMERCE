package com.marketplace.solicitud.service;

import com.marketplace.solicitud.config.ParamSnapshot;
import com.marketplace.solicitud.config.SolicitudParametrosOperativos;
import com.marketplace.solicitud.entity.Solicitud;
import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.notification.SolicitudEstadoNotificador;
import com.marketplace.solicitud.repository.CalificacionVendedorRepository;
import com.marketplace.solicitud.repository.SolicitudRepository;
import com.marketplace.solicitud.state.SolicitudEstadoRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Evalúa reputación y cancela solicitudes ACTIVA/EN_MORA si se cumple la política §6.
 */
@Service
public class ReputacionMantenimientoService {

    private static final Logger log = LoggerFactory.getLogger(ReputacionMantenimientoService.class);

    private static final int NOTA_EXCLUSIVA_MALA = 3;

    private final SolicitudRepository solicitudRepository;
    private final CalificacionVendedorRepository calificacionVendedorRepository;
    private final SolicitudEstadoRegistry estadoRegistry;
    private final SolicitudEstadoNotificador solicitudEstadoNotificador;
    private final SolicitudParametrosOperativos solicitudParametrosOperativos;

    public ReputacionMantenimientoService(
            SolicitudRepository solicitudRepository,
            CalificacionVendedorRepository calificacionVendedorRepository,
            SolicitudEstadoRegistry estadoRegistry,
            SolicitudEstadoNotificador solicitudEstadoNotificador,
            SolicitudParametrosOperativos solicitudParametrosOperativos) {
        this.solicitudRepository = solicitudRepository;
        this.calificacionVendedorRepository = calificacionVendedorRepository;
        this.estadoRegistry = estadoRegistry;
        this.solicitudEstadoNotificador = solicitudEstadoNotificador;
        this.solicitudParametrosOperativos = solicitudParametrosOperativos;
    }

    @Transactional
    public void ejecutarCiclo() {
        List<Solicitud> candidatas =
                solicitudRepository.findByEstadoIn(EnumSet.of(SolicitudEstado.ACTIVA, SolicitudEstado.EN_MORA));
        for (Solicitud s : candidatas) {
            try {
                evaluarYCancelarSiAplica(s);
            } catch (Exception ex) {
                log.warn("Reputación: no se pudo evaluar solicitud id={}: {}", s.getId(), ex.getMessage());
            }
        }
    }

    private void evaluarYCancelarSiAplica(Solicitud s) {
        Long id = s.getId();
        ParamSnapshot p = solicitudParametrosOperativos.actual();
        long malas = calificacionVendedorRepository.countBySolicitud_IdAndValorLessThan(id, NOTA_EXCLUSIVA_MALA);
        Optional<Double> promedio = calificacionVendedorRepository.promedioValorPorSolicitud(id);

        boolean porMalas = malas >= p.umbralCalificacionesMalas();
        boolean porPromedio = promedio.isPresent() && promedio.get() < p.umbralPromedio();
        if (!porMalas && !porPromedio) {
            return;
        }

        estadoRegistry.comportamientoPara(s.getEstado()).assertTransicionPermitida(SolicitudEstado.CANCELADA);
        s.setEstado(SolicitudEstado.CANCELADA);
        solicitudRepository.save(s);

        String detalle =
                porMalas && porPromedio
                        ? "malas=%d (umbral=%d), promedio=%.2f (umbral=%.2f)"
                                .formatted(
                                        malas,
                                        p.umbralCalificacionesMalas(),
                                        promedio.orElse(0d),
                                        p.umbralPromedio())
                        : porMalas
                                ? "malas=%d (umbral=%d)"
                                        .formatted(malas, p.umbralCalificacionesMalas())
                                : "promedio=%.2f (umbral=%.2f)"
                                        .formatted(promedio.orElse(0d), p.umbralPromedio());

        solicitudEstadoNotificador.notificarCancelacionPorReputacion(
                id, s.getNombreVendedor(), s.getCorreoElectronico(), detalle);
        log.info("Reputación (caso estudio) → CANCELADA (solicitud id={}, {})", id, detalle);
    }
}
