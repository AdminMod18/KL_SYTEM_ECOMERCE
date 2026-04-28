package com.marketplace.solicitud.chain;

import com.marketplace.solicitud.dto.SolicitudCreateRequest;
import com.marketplace.solicitud.exception.SolicitudBusinessException;
import com.marketplace.solicitud.repository.SolicitudRepository;
import org.springframework.stereotype.Component;

/**
 * Propósito: impedir registros duplicados por documento de identidad.
 * Patrón: Chain of Responsibility (manejador concreto).
 * Responsabilidad: consultar repositorio como último paso previo a crear la entidad.
 */
@Component
public class DocumentoNoDuplicadoHandler extends SolicitudValidationHandler {

    private final SolicitudRepository solicitudRepository;

    public DocumentoNoDuplicadoHandler(SolicitudRepository solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    @Override
    protected void procesar(SolicitudCreateRequest solicitud) {
        String doc = solicitud.getDocumentoIdentidad().trim();
        if (solicitudRepository.existsByDocumentoIdentidad(doc)) {
            throw new SolicitudBusinessException("DOCUMENTO_DUPLICADO", "Ya existe una solicitud con ese documento.");
        }
    }
}
