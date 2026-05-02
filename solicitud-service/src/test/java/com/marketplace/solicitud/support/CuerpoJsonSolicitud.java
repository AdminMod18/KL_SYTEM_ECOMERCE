package com.marketplace.solicitud.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.solicitud.builder.SolicitudRegistroBuilder;

/**
 * Propósito: JSON consistente para POST /solicitudes en pruebas (persona natural demo).
 * Patrón: Object Mother / test fixture.
 * Responsabilidad: evitar duplicar payloads grandes en cada {@code MockMvc} test.
 */
public final class CuerpoJsonSolicitud {

    private CuerpoJsonSolicitud() {}

    public static String crearNatural(ObjectMapper mapper, String documento, String nombres, String apellidos)
            throws JsonProcessingException {
        return mapper.writeValueAsString(SolicitudRegistroBuilder.demoNatural(documento, nombres, apellidos));
    }

    public static String crearNaturalNombreMostrar(
            ObjectMapper mapper,
            String documento,
            String nombreVendedor,
            String nombres,
            String apellidos)
            throws JsonProcessingException {
        return mapper.writeValueAsString(
                SolicitudRegistroBuilder.demoNaturalNombreMostrar(documento, nombreVendedor, nombres, apellidos));
    }
}
