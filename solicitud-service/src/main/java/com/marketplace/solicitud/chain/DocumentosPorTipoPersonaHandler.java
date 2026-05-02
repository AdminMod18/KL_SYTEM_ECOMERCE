package com.marketplace.solicitud.chain;

import com.marketplace.solicitud.dto.AdjuntoCreateDto;
import com.marketplace.solicitud.dto.SolicitudCreateRequest;
import com.marketplace.solicitud.exception.SolicitudBusinessException;
import com.marketplace.solicitud.model.TipoDocumentoAdjunto;
import com.marketplace.solicitud.model.TipoPersonaSolicitante;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Propósito: exigir el paquete documental mínimo según persona natural o jurídica.
 * Patrón: Chain of Responsibility (manejador concreto).
 * Responsabilidad: validar cobertura de tipos de adjunto antes del formato de documento de identidad.
 */
@Component
public class DocumentosPorTipoPersonaHandler extends SolicitudValidationHandler {

    private static final Set<TipoDocumentoAdjunto> NATURAL_REQUERIDOS =
            EnumSet.of(
                    TipoDocumentoAdjunto.CEDULA,
                    TipoDocumentoAdjunto.ACEPTACION_CENTRALES_RIESGO,
                    TipoDocumentoAdjunto.ACEPTACION_DATOS_PERSONALES);

    private static final Set<TipoDocumentoAdjunto> JURIDICA_REQUERIDOS =
            EnumSet.of(
                    TipoDocumentoAdjunto.RUT,
                    TipoDocumentoAdjunto.CAMARA_COMERCIO,
                    TipoDocumentoAdjunto.ACEPTACION_CENTRALES_RIESGO,
                    TipoDocumentoAdjunto.ACEPTACION_DATOS_PERSONALES);

    @Override
    protected void procesar(SolicitudCreateRequest solicitud) {
        Set<TipoDocumentoAdjunto> presentes =
                solicitud.getAdjuntos().stream().map(AdjuntoCreateDto::getTipo).collect(Collectors.toSet());

        Set<TipoDocumentoAdjunto> requeridos =
                solicitud.getTipoPersona() == TipoPersonaSolicitante.NATURAL
                        ? NATURAL_REQUERIDOS
                        : JURIDICA_REQUERIDOS;

        if (!presentes.containsAll(requeridos)) {
            EnumSet<TipoDocumentoAdjunto> faltan = EnumSet.copyOf(requeridos);
            faltan.removeAll(presentes);
            throw new SolicitudBusinessException(
                    "ADJUNTOS_INCOMPLETOS",
                    "Faltan tipos de documento obligatorios para "
                            + solicitud.getTipoPersona()
                            + ": "
                            + faltan);
        }
    }
}
