package com.marketplace.solicitud.chain;

import com.marketplace.solicitud.dto.SolicitudCreateRequest;
import com.marketplace.solicitud.exception.SolicitudBusinessException;
import org.springframework.stereotype.Component;

/**
 * Propósito: garantizar que nombre y documento no estén vacíos tras trim (reglas adicionales a Bean Validation).
 * Patrón: Chain of Responsibility (manejador concreto).
 * Responsabilidad: primer filtro semántico en la cadena de creación de solicitudes.
 */
@Component
public class CamposObligatoriosHandler extends SolicitudValidationHandler {

    @Override
    protected void procesar(SolicitudCreateRequest solicitud) {
        if (solicitud.getNombreVendedor() == null || solicitud.getNombreVendedor().isBlank()) {
            throw new SolicitudBusinessException("NOMBRE_REQUERIDO", "El nombre del vendedor es obligatorio.");
        }
        if (solicitud.getDocumentoIdentidad() == null || solicitud.getDocumentoIdentidad().isBlank()) {
            throw new SolicitudBusinessException("DOCUMENTO_REQUERIDO", "El documento de identidad es obligatorio.");
        }
    }
}
