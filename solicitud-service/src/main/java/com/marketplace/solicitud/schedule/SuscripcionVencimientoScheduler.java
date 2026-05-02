package com.marketplace.solicitud.schedule;

import com.marketplace.solicitud.service.SuscripcionMantenimientoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job que aplica mora y cancelación por suscripción vencida (patrón State fuera del hilo HTTP).
 */
@Component
@ConditionalOnProperty(
        prefix = "solicitud.suscripcion",
        name = "scheduler-habilitado",
        havingValue = "true",
        matchIfMissing = true)
public class SuscripcionVencimientoScheduler {

    private static final Logger log = LoggerFactory.getLogger(SuscripcionVencimientoScheduler.class);

    private final SuscripcionMantenimientoService mantenimientoService;

    public SuscripcionVencimientoScheduler(SuscripcionMantenimientoService mantenimientoService) {
        this.mantenimientoService = mantenimientoService;
    }

    @Scheduled(fixedDelayString = "${solicitud.suscripcion.job-fixed-delay-ms:3600000}")
    public void ejecutarRevisionSuscripciones() {
        log.debug("Job suscripción: revisando vencimientos y mora");
        mantenimientoService.ejecutarCiclo();
    }
}
