package com.marketplace.solicitud.chain;

import com.marketplace.solicitud.dto.SolicitudCreateRequest;
import com.marketplace.solicitud.exception.SolicitudBusinessException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Propósito: reglas de formato de teléfono de contacto (ligero, sin homologación por país).
 * Patrón: Chain of Responsibility (manejador concreto).
 * Responsabilidad: rechazar valores demasiado cortos o con caracteres no permitidos.
 */
@Component
public class TelefonoBasicoHandler extends SolicitudValidationHandler {

    private static final Pattern TELEFONO = Pattern.compile("^[0-9+()\\-\\s]{7,40}$");

    @Override
    protected void procesar(SolicitudCreateRequest solicitud) {
        String tel = solicitud.getTelefono().trim();
        if (!TELEFONO.matcher(tel).matches()) {
            throw new SolicitudBusinessException(
                    "TELEFONO_FORMATO",
                    "El teléfono debe tener entre 7 y 40 caracteres y solo dígitos, espacios, +, () y guiones.");
        }
    }
}
