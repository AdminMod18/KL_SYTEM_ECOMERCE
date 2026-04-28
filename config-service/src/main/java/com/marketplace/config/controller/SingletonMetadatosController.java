package com.marketplace.config.controller;

import com.marketplace.config.singleton.ConfigServiceRegistrySingleton;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Propósito: exponer metadatos del Singleton de registro del config-service (verificación del patrón).
 * Patrón: Adapter HTTP (no es el Singleton; solo consulta {@link ConfigServiceRegistrySingleton#INSTANCE}).
 * Responsabilidad: devolver datos de solo lectura del registro global del proceso.
 */
@Tag(name = "Demostración Singleton", description = "Metadatos del registro global del config-service")
@RestController
@RequestMapping("/singleton")
public class SingletonMetadatosController {

    @Operation(summary = "Metadatos del singleton de registro")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Nombre, versión e instante de inicio")})
    @GetMapping("/metadatos")
    public MetadatosSingletonResponse metadatos(
            @Parameter(name = "X-Request-Id", description = "Identificador de correlación opcional", in = ParameterIn.HEADER, required = false)
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        ConfigServiceRegistrySingleton s = ConfigServiceRegistrySingleton.INSTANCE;
        return new MetadatosSingletonResponse(s.getNombreServicio(), s.getVersion(), s.getIniciadoEn());
    }

    public record MetadatosSingletonResponse(String nombreServicio, String version, Instant iniciadoEn) {
    }
}
