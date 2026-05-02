package com.marketplace.analytics.service;

import com.marketplace.analytics.dto.EventoMetricaRequest;
import com.marketplace.analytics.dto.EventoMetricaResponse;
import com.marketplace.analytics.dto.KpiResponse;
import com.marketplace.analytics.entity.EventoMetrica;
import com.marketplace.analytics.model.TipoEventoMetrica;
import com.marketplace.analytics.repository.EventoMetricaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Propósito: registrar eventos y calcular KPIs agregados sobre la base de hechos.
 * Patrón: Application Service (CQRS-lite: escritura de eventos + lectura de KPIs).
 * Responsabilidad: persistir eventos y consultar agregados vía repositorio.
 */
@Service
public class AnalyticsService {

    private final EventoMetricaRepository eventoMetricaRepository;

    public AnalyticsService(EventoMetricaRepository eventoMetricaRepository) {
        this.eventoMetricaRepository = eventoMetricaRepository;
    }

    @Transactional
    public EventoMetricaResponse registrarEvento(EventoMetricaRequest request) {
        EventoMetrica evento = new EventoMetrica();
        evento.setTipo(request.getTipo());
        evento.setReferencia(request.getReferencia().trim());
        evento.setValorMonetario(request.getValorMonetario());
        EventoMetrica guardado = eventoMetricaRepository.save(evento);
        return new EventoMetricaResponse(
                guardado.getId(),
                guardado.getTipo(),
                guardado.getReferencia(),
                guardado.getValorMonetario(),
                guardado.getOcurridoEn());
    }

    @Transactional(readOnly = true)
    public KpiResponse obtenerKpis() {
        long total = eventoMetricaRepository.count();
        long compras = eventoMetricaRepository.countByTipo(TipoEventoMetrica.COMPRA);
        BigDecimal ingresos = eventoMetricaRepository.sumValorMonetarioByTipo(TipoEventoMetrica.COMPRA);
        long solicitudes = eventoMetricaRepository.countByTipo(TipoEventoMetrica.SOLICITUD_APROBADA);
        long consultasCat = eventoMetricaRepository.countByTipo(TipoEventoMetrica.CONSULTA_CATALOGO);
        String topSku = eventoMetricaRepository.findReferenciaMasFrecuentePorTipo(TipoEventoMetrica.COMPRA.name());
        String topConsulta =
                eventoMetricaRepository.findReferenciaMasFrecuentePorTipo(TipoEventoMetrica.CONSULTA_CATALOGO.name());
        Instant ultimo = eventoMetricaRepository.findMaxOcurridoEn();
        String marketing =
                construirResumenMarketing(topSku, topConsulta, consultasCat);
        return new KpiResponse(
                total, compras, ingresos, solicitudes, consultasCat, topSku, topConsulta, ultimo, marketing);
    }

    private static String construirResumenMarketing(String topSku, String topConsulta, long consultasCat) {
        if ((topSku == null || topSku.isBlank()) && (topConsulta == null || topConsulta.isBlank())) {
            return consultasCat > 0
                    ? "Hay consultas de catálogo registradas; registre más eventos COMPRA para SKU top."
                    : "Sin datos de tendencias aún. Envíe eventos CONSULTA_CATALOGO y COMPRA desde el storefront.";
        }
        return "SKU más repetido en compras: "
                + (topSku != null ? topSku : "—")
                + "; consulta/catálogo más frecuente: "
                + (topConsulta != null ? topConsulta : "—")
                + ". Base para campañas (HU-24).";
    }
}
