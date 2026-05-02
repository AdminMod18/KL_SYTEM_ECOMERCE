package com.marketplace.solicitud.dto;

import com.marketplace.solicitud.model.TipoDocumentoAdjunto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Propósito: describir un archivo adjunto en la creación de solicitud (metadatos y contenido opcional).
 * Patrón: DTO anidado validado con Bean Validation.
 * Responsabilidad: transportar tipo, nombre y Base64 opcional sin acoplar a JPA.
 */
@Getter
@Setter
public class AdjuntoCreateDto {

    @NotNull
    private TipoDocumentoAdjunto tipo;

    @NotBlank
    @Size(max = 260)
    private String nombreArchivo;

    /**
     * Opcional: si viene, {@link com.marketplace.solicitud.storage.AdjuntosSolicitudFacade} persiste en disco.
     */
    private String contenidoBase64;
}
