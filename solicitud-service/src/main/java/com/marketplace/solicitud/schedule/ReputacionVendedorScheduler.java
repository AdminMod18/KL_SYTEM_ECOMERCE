package com.marketplace.solicitud.schedule;

import com.marketplace.solicitud.service.ReputacionMantenimientoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job que aplica cancelación por reputación (§6) fuera del hilo HTTP.
 */
@Component
@ConditionalOnProperty(
        prefix = "solicitud.reputacion",
        name = "scheduler-habilitado",
        havingValue = "true",
        matchIfMissing = true)
public class ReputacionVendedorScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReputacionVendedorScheduler.class);

    private final ReputacionMantenimientoService reputacionMantenimientoService;

    public ReputacionVendedorScheduler(ReputacionMantenimientoService reputacionMantenimientoService) {
        this.reputacionMantenimientoService = reputacionMantenimientoService;
    }

    @Scheduled(fixedDelayString = "${solicitud.reputacion.job-fixed-delay-ms:3600000}")
    public void ejecutarRevisionReputacion() {
        log.debug("Job reputación: evaluando cancelaciones §6");
        reputacionMantenimientoService.ejecutarCiclo();
    }
}
