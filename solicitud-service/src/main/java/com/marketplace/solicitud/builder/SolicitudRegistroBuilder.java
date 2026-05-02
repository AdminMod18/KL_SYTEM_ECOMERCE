package com.marketplace.solicitud.builder;

import com.marketplace.solicitud.dto.AdjuntoCreateDto;
import com.marketplace.solicitud.dto.SolicitudCreateRequest;
import com.marketplace.solicitud.model.TipoDocumentoAdjunto;
import com.marketplace.solicitud.model.TipoPersonaSolicitante;

import java.util.ArrayList;
import java.util.List;

/**
 * Propósito: armar solicitudes de alta con API fluida (demos, seeds, integraciones).
 * Patrón: Builder (construcción paso a paso de un objeto compuesto).
 * Responsabilidad: reducir errores al poblar listas de adjuntos y campos repetidos en tests.
 */
public final class SolicitudRegistroBuilder {

    private final SolicitudCreateRequest target = new SolicitudCreateRequest();
    private final List<AdjuntoCreateDto> adjuntos = new ArrayList<>();

    public SolicitudRegistroBuilder nombreMostrarOpcional(String nombreVendedor) {
        target.setNombreVendedor(nombreVendedor);
        return this;
    }

    public SolicitudRegistroBuilder nombres(String v) {
        target.setNombres(v);
        return this;
    }

    public SolicitudRegistroBuilder apellidos(String v) {
        target.setApellidos(v);
        return this;
    }

    public SolicitudRegistroBuilder documentoIdentidad(String v) {
        target.setDocumentoIdentidad(v);
        return this;
    }

    public SolicitudRegistroBuilder correo(String v) {
        target.setCorreoElectronico(v);
        return this;
    }

    public SolicitudRegistroBuilder pais(String v) {
        target.setPaisResidencia(v);
        return this;
    }

    public SolicitudRegistroBuilder ciudad(String v) {
        target.setCiudadResidencia(v);
        return this;
    }

    public SolicitudRegistroBuilder telefono(String v) {
        target.setTelefono(v);
        return this;
    }

    public SolicitudRegistroBuilder tipoPersona(TipoPersonaSolicitante v) {
        target.setTipoPersona(v);
        return this;
    }

    public SolicitudRegistroBuilder adjunto(TipoDocumentoAdjunto tipo, String nombreArchivo) {
        AdjuntoCreateDto dto = new AdjuntoCreateDto();
        dto.setTipo(tipo);
        dto.setNombreArchivo(nombreArchivo);
        adjuntos.add(dto);
        return this;
    }

    public SolicitudCreateRequest build() {
        target.setAdjuntos(new ArrayList<>(adjuntos));
        return target;
    }

    /**
     * Paquete mínimo persona natural con tres PDF de ejemplo (solo metadatos).
     */
    public static SolicitudCreateRequest demoNatural(String documento, String nombres, String apellidos) {
        return demoNaturalInterno(documento, null, nombres, apellidos);
    }

    /**
     * Igual que {@link #demoNatural(String, String, String)} pero fija {@code nombreVendedor} explícito para integraciones mock.
     */
    public static SolicitudCreateRequest demoNaturalNombreMostrar(
            String documento, String nombreVendedor, String nombres, String apellidos) {
        return demoNaturalInterno(documento, nombreVendedor, nombres, apellidos);
    }

    private static SolicitudCreateRequest demoNaturalInterno(
            String documento, String nombreVendedorOpcional, String nombres, String apellidos) {
        SolicitudRegistroBuilder b =
                new SolicitudRegistroBuilder()
                        .nombres(nombres)
                        .apellidos(apellidos)
                        .documentoIdentidad(documento)
                        .correo("vendedor-demo@marketplace.local")
                        .pais("Colombia")
                        .ciudad("Bogotá")
                        .telefono("3001234567")
                        .tipoPersona(TipoPersonaSolicitante.NATURAL)
                        .adjunto(TipoDocumentoAdjunto.CEDULA, "cedula.pdf")
                        .adjunto(TipoDocumentoAdjunto.ACEPTACION_CENTRALES_RIESGO, "centrales-riesgo.pdf")
                        .adjunto(TipoDocumentoAdjunto.ACEPTACION_DATOS_PERSONALES, "datos-personales.pdf");
        if (nombreVendedorOpcional != null && !nombreVendedorOpcional.isBlank()) {
            b.nombreMostrarOpcional(nombreVendedorOpcional);
        }
        return b.build();
    }
}
