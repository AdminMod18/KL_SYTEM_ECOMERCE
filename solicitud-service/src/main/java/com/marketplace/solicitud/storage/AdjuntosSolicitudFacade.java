package com.marketplace.solicitud.storage;

import com.marketplace.solicitud.exception.SolicitudBusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Propósito: persistir bytes de adjuntos en filesystem local de forma uniforme.
 * Patrón: Facade (API simple sobre IO y rutas).
 * Responsabilidad: aislar detalles de almacenamiento del caso de uso de creación de solicitud.
 */
@Component
public class AdjuntosSolicitudFacade {

    private static final Pattern SEGURO_NOMBRE = Pattern.compile("^[^\\\\/:*?\"<>|]+$");

    private final Path basePath;

    public AdjuntosSolicitudFacade(
            @Value("${solicitud.adjuntos.base-path:}") String configurado) {
        if (configurado == null || configurado.isBlank()) {
            this.basePath =
                    Paths.get(System.getProperty("java.io.tmpdir"), "marketplace-solicitud-adjuntos")
                            .toAbsolutePath();
        } else {
            this.basePath = Paths.get(configurado).toAbsolutePath();
        }
    }

    /**
     * @return ruta absoluta guardada, o {@code null} si no hay contenido
     */
    public String persistirSiHayContenido(Long solicitudId, String nombreOriginal, String contenidoBase64) {
        if (contenidoBase64 == null || contenidoBase64.isBlank()) {
            return null;
        }
        String nombre = nombreOriginal == null ? "adjunto" : nombreOriginal.trim();
        if (nombre.isEmpty() || !SEGURO_NOMBRE.matcher(nombre).matches()) {
            throw new SolicitudBusinessException(
                    "ADJUNTO_NOMBRE_INVALIDO", "nombreArchivo tiene caracteres no permitidos para almacenamiento.");
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(contenidoBase64.trim());
        } catch (IllegalArgumentException ex) {
            throw new SolicitudBusinessException("ADJUNTO_BASE64_INVALIDO", "contenidoBase64 no es Base64 válido.");
        }
        try {
            Path dir = basePath.resolve("solicitud-" + solicitudId);
            Files.createDirectories(dir);
            String fileName = UUID.randomUUID() + "_" + nombre;
            Path destino = dir.resolve(fileName);
            Files.write(destino, bytes);
            return destino.toString();
        } catch (IOException ex) {
            throw new SolicitudBusinessException(
                    "ADJUNTO_ALMACENAMIENTO_ERROR", "No se pudo guardar el adjunto: " + ex.getMessage());
        }
    }
}
