package com.marketplace.solicitud.chain;

import com.marketplace.solicitud.dto.AdjuntoCreateDto;
import com.marketplace.solicitud.dto.SolicitudCreateRequest;
import com.marketplace.solicitud.exception.SolicitudBusinessException;
import org.springframework.stereotype.Component;

/**
 * Propósito: garantizar datos mínimos coherentes antes de reglas de formato y duplicados.
 * Patrón: Chain of Responsibility (manejador concreto).
 * Responsabilidad: primer filtro semántico en la cadena de creación de solicitudes (mensajes de dominio estables).
 */
@Component
public class CamposObligatoriosHandler extends SolicitudValidationHandler {

    @Override
    protected void procesar(SolicitudCreateRequest solicitud) {
        if (solicitud.getNombres() == null || solicitud.getNombres().isBlank()) {
            throw new SolicitudBusinessException("NOMBRES_REQUERIDO", "Los nombres del solicitante son obligatorios.");
        }
        if (solicitud.getApellidos() == null || solicitud.getApellidos().isBlank()) {
            throw new SolicitudBusinessException("APELLIDOS_REQUERIDO", "Los apellidos del solicitante son obligatorios.");
        }
        if (solicitud.getDocumentoIdentidad() == null || solicitud.getDocumentoIdentidad().isBlank()) {
            throw new SolicitudBusinessException("DOCUMENTO_REQUERIDO", "El documento de identidad es obligatorio.");
        }
        if (solicitud.getCorreoElectronico() == null || solicitud.getCorreoElectronico().isBlank()) {
            throw new SolicitudBusinessException("CORREO_REQUERIDO", "El correo electrónico es obligatorio.");
        }
        if (solicitud.getPaisResidencia() == null || solicitud.getPaisResidencia().isBlank()) {
            throw new SolicitudBusinessException("PAIS_REQUERIDO", "El país de residencia es obligatorio.");
        }
        if (solicitud.getCiudadResidencia() == null || solicitud.getCiudadResidencia().isBlank()) {
            throw new SolicitudBusinessException("CIUDAD_REQUERIDO", "La ciudad de residencia es obligatoria.");
        }
        if (solicitud.getTelefono() == null || solicitud.getTelefono().isBlank()) {
            throw new SolicitudBusinessException("TELEFONO_REQUERIDO", "El teléfono es obligatorio.");
        }
        if (solicitud.getTipoPersona() == null) {
            throw new SolicitudBusinessException("TIPO_PERSONA_REQUERIDO", "El tipo de persona es obligatorio.");
        }
        if (solicitud.getAdjuntos() == null || solicitud.getAdjuntos().isEmpty()) {
            throw new SolicitudBusinessException("ADJUNTOS_REQUERIDO", "Debe adjuntar al menos un documento.");
        }
        for (AdjuntoCreateDto adj : solicitud.getAdjuntos()) {
            if (adj.getTipo() == null) {
                throw new SolicitudBusinessException("ADJUNTO_TIPO_REQUERIDO", "Cada adjunto debe tener un tipo.");
            }
            if (adj.getNombreArchivo() == null || adj.getNombreArchivo().isBlank()) {
                throw new SolicitudBusinessException(
                        "ADJUNTO_NOMBRE_REQUERIDO", "Cada adjunto debe tener nombreArchivo.");
            }
        }
    }
}
