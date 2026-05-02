package com.marketplace.admin.service;

import com.marketplace.admin.dto.AuditoriaItemResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AuditoriaDemoService {

    public List<AuditoriaItemResponse> listar() {
        return List.of(
                new AuditoriaItemResponse(
                        Instant.parse("2026-04-20T10:15:00Z"),
                        "director.demo",
                        "CAMBIO_PARAMETRO",
                        "Actualizo solicitud.reputacion.umbral-promedio"),
                new AuditoriaItemResponse(
                        Instant.parse("2026-04-19T08:00:00Z"),
                        "sistema",
                        "JOB_SUSCRIPCION",
                        "Ciclo mora ejecutado (mock)"));
    }
}
