package com.marketplace.solicitud.service;

import com.marketplace.solicitud.config.SolicitudParametrosOperativos;
import com.marketplace.solicitud.entity.Solicitud;
import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.notification.SolicitudEstadoNotificador;
import com.marketplace.solicitud.repository.SolicitudRepository;
import com.marketplace.solicitud.state.SolicitudEstadoRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Propósito: aplicar transiciones automáticas ACTIVA→EN_MORA y EN_MORA→CANCELADA según fechas de suscripción.
 * Patrón: aplicación del State pattern fuera del request HTTP (job).
 */
@Service
public class SuscripcionMantenimientoService {

    private static final Logger log = LoggerFactory.getLogger(SuscripcionMantenimientoService.class);

    private final SolicitudRepository solicitudRepository;
    private final SolicitudEstadoRegistry estadoRegistry;
    private final SolicitudEstadoNotificador solicitudEstadoNotificador;
    private final SolicitudParametrosOperativos solicitudParametrosOperativos;
    private final Clock clock;

    public SuscripcionMantenimientoService(
            SolicitudRepository solicitudRepository,
            SolicitudEstadoRegistry estadoRegistry,
            SolicitudEstadoNotificador solicitudEstadoNotificador,
            SolicitudParametrosOperativos solicitudParametrosOperativos,
            Clock clock) {
        this.solicitudRepository = solicitudRepository;
        this.estadoRegistry = estadoRegistry;
        this.solicitudEstadoNotificador = solicitudEstadoNotificador;
        this.solicitudParametrosOperativos = solicitudParametrosOperativos;
        this.clock = clock;
    }

    /**
     * Una pasada del ciclo: primero mora por vencimiento (tras demora configurable), luego cancelación tras gracia en mora.
     */
    @Transactional
    public void ejecutarCiclo() {
        Instant ahora = clock.instant();
        aplicarMoraPorVencimiento(ahora);
        aplicarCancelacionPorMoraProlongada(ahora);
    }

    private void aplicarMoraPorVencimiento(Instant ahora) {
        Instant umbral = ahora.minus(solicitudParametrosOperativos.actual().demoraAntesMora());
        List<Solicitud> candidatas =
                solicitudRepository.findByEstadoAndProximoVencimientoSuscripcionIsNotNullAndProximoVencimientoSuscripcionBefore(
                        SolicitudEstado.ACTIVA, umbral);
        for (Solicitud s : candidatas) {
            try {
                estadoRegistry.comportamientoPara(s.getEstado()).assertTransicionPermitida(SolicitudEstado.EN_MORA);
                s.setEstado(SolicitudEstado.EN_MORA);
                s.setEntradaEnMoraEn(ahora);
                solicitudRepository.save(s);
                solicitudEstadoNotificador.notificarSuscripcion(
                        SolicitudEstado.EN_MORA,
                        s.getId(),
                        s.getNombreVendedor(),
                        s.getCorreoElectronico(),
                        s.getProximoVencimientoSuscripcion());
                log.info(
                        "Suscripción vencida → EN_MORA (solicitud id={}, vencimiento={}, entradaEnMoraEn={})",
                        s.getId(),
                        s.getProximoVencimientoSuscripcion(),
                        s.getEntradaEnMoraEn());
            } catch (Exception ex) {
                log.warn("No se pudo pasar a EN_MORA solicitud id={}: {}", s.getId(), ex.getMessage());
            }
        }
    }

    private void aplicarCancelacionPorMoraProlongada(Instant ahora) {
        List<Solicitud> enMora =
                solicitudRepository.findByEstadoAndProximoVencimientoSuscripcionIsNotNull(SolicitudEstado.EN_MORA);
        for (Solicitud s : enMora) {
            Instant vencimiento = s.getProximoVencimientoSuscripcion();
            Instant limiteCancelacion;
            if (s.getEntradaEnMoraEn() != null) {
                limiteCancelacion =
                        s.getEntradaEnMoraEn()
                                .plus(solicitudParametrosOperativos.actual().graciaCancelacionDesdeMora());
            } else {
                limiteCancelacion =
                        vencimiento.plus(
                                solicitudParametrosOperativos.actual().graciaCancelacionLegacySinFechaMora());
            }
            if (ahora.isBefore(limiteCancelacion)) {
                continue;
            }
            try {
                estadoRegistry.comportamientoPara(s.getEstado()).assertTransicionPermitida(SolicitudEstado.CANCELADA);
                s.setEstado(SolicitudEstado.CANCELADA);
                solicitudRepository.save(s);
                solicitudEstadoNotificador.notificarSuscripcion(
                        SolicitudEstado.CANCELADA,
                        s.getId(),
                        s.getNombreVendedor(),
                        s.getCorreoElectronico(),
                        vencimiento);
                log.info(
                        "Mora prolongada → CANCELADA (solicitud id={}, limite={}, ahora={})",
                        s.getId(),
                        limiteCancelacion,
                        ahora);
            } catch (Exception ex) {
                log.warn("No se pudo cancelar solicitud id={}: {}", s.getId(), ex.getMessage());
            }
        }
    }
}
