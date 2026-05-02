package com.marketplace.solicitud.command;

import com.marketplace.solicitud.chain.SolicitudValidationHandler;
import com.marketplace.solicitud.dto.SolicitudCreateRequest;

/**
 * Propósito: encapsular la validación previa a persistir una solicitud nueva.
 * Patrón: Command (operación reversible solo en sentido de orquestación; ejecutar dispara efectos colaterales vía excepciones).
 * Responsabilidad: invocar la cadena CoR sin que el servicio acople la secuencia interna.
 */
public class ValidacionCreacionSolicitudCommand {

    private final SolicitudValidationHandler cadena;
    private final SolicitudCreateRequest payload;

    public ValidacionCreacionSolicitudCommand(SolicitudValidationHandler cadena, SolicitudCreateRequest payload) {
        this.cadena = cadena;
        this.payload = payload;
    }

    public void ejecutar() {
        cadena.validar(payload);
    }
}
