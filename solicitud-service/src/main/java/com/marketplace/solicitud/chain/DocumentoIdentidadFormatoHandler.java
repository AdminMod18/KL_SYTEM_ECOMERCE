package com.marketplace.solicitud.chain;

import com.marketplace.solicitud.dto.SolicitudCreateRequest;
import com.marketplace.solicitud.exception.SolicitudBusinessException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Propósito: validar formato alfanumérico del documento (sustituto local de reglas de homologación externa).
 * Patrón: Chain of Responsibility (manejador concreto).
 * Responsabilidad: aplicar reglas de formato antes de comprobar duplicados o persistencia.
 */
@Component
public class DocumentoIdentidadFormatoHandler extends SolicitudValidationHandler {

    private static final Pattern DOCUMENTO = Pattern.compile("^[A-Za-z0-9]{5,32}$");

    @Override
    protected void procesar(SolicitudCreateRequest solicitud) {
        String doc = solicitud.getDocumentoIdentidad().trim();
        if (!DOCUMENTO.matcher(doc).matches()) {
            throw new SolicitudBusinessException(
                    "DOCUMENTO_FORMATO",
                    "El documento debe tener entre 5 y 32 caracteres alfanuméricos.");
        }
    }
}
