package com.marketplace.user.dto.interno;

import jakarta.validation.constraints.AssertTrue;

/**
 * Cuerpo para promoción automática tras activación comercial (solicitud-service → ACTIVA).
 * Debe coincidir documento y/o correo con el usuario ya registrado como comprador.
 */
public class PromoverVendedorRequest {

    public PromoverVendedorRequest() {}

    private String documentoIdentidad;
    private String correoElectronico;

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public void setDocumentoIdentidad(String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    @AssertTrue(message = "Indique documentoIdentidad y/o correoElectronico.")
    public boolean isDocumentoOCorreo() {
        boolean doc = documentoIdentidad != null && !documentoIdentidad.isBlank();
        boolean mail = correoElectronico != null && !correoElectronico.isBlank();
        return doc || mail;
    }
}
