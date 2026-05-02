package com.marketplace.admin.config;

import com.marketplace.admin.entity.ParametroSistema;
import com.marketplace.admin.repository.ParametroSistemaRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Semilla minimal de parametros alineados al marketplace (valores ilustrativos).
 */
@Configuration
public class AdminSeedRunner {

    @Bean
    ApplicationRunner seedParametros(ParametroSistemaRepository repo) {
        return args -> {
            upsert(repo, "solicitud.suscripcion.demora-antes-mora", "P1D");
            upsert(repo, "solicitud.suscripcion.gracia-cancelacion-desde-mora", "P30D");
            upsert(repo, "solicitud.reputacion.umbral-calificaciones-malas", "10");
            upsert(repo, "solicitud.reputacion.umbral-promedio", "5.0");
            upsert(repo, "order.pricing.commission-rate", "0.05");
        };
    }

    private static void upsert(ParametroSistemaRepository repo, String clave, String valor) {
        repo.findByClave(clave)
                .map(p -> {
                    p.setValor(valor);
                    return repo.save(p);
                })
                .orElseGet(() -> {
                    ParametroSistema n = new ParametroSistema();
                    n.setClave(clave);
                    n.setValor(valor);
                    return repo.save(n);
                });
    }
}
