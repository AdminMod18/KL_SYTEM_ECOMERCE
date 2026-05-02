package com.marketplace.solicitud.schedule;

import com.marketplace.solicitud.integration.AdminParametroSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Reintenta traer parámetros desde admin-service periódicamente (si hay URL configurada).
 */
@Component
public class AdminParametroSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(AdminParametroSyncScheduler.class);

    private final AdminParametroSyncService adminParametroSyncService;

    public AdminParametroSyncScheduler(AdminParametroSyncService adminParametroSyncService) {
        this.adminParametroSyncService = adminParametroSyncService;
    }

    @Scheduled(fixedDelayString = "${integracion.admin.sync-fixed-delay-ms:300000}")
    public void ejecutarSincronizacion() {
        log.trace("Job admin: sincronización de parámetros operativos");
        adminParametroSyncService.sincronizarSiCorresponde();
    }
}
