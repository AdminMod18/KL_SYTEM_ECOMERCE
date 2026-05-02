package com.marketplace.auth.controller;

import com.marketplace.auth.config.OpenApiConfig;
import com.marketplace.auth.dto.LoginRequest;
import com.marketplace.auth.dto.LoginResponse;
import com.marketplace.auth.dto.RolesResponse;
import com.marketplace.auth.dto.SincronizarVendedorRequest;
import com.marketplace.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Propósito: API REST de login y consulta de roles a partir de JWT.
 * Patrón: Adapter HTTP.
 * Responsabilidad: validar DTOs y delegar en {@link AuthService} (proxy primario).
 */
@Tag(name = "Autenticación", description = "Login JWT y consulta de roles")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Iniciar sesión", description = "Valida credenciales y devuelve un JWT de acceso.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación correcta"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas", content = @Content)
    })
    @PostMapping("/login")
    public LoginResponse login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Usuario y contraseña", required = true)
            @Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Obtener roles", description = "Parsea el JWT del encabezado Authorization y devuelve los roles.")
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_BEARER)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles obtenidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido", content = @Content)
    })
    @GetMapping("/roles")
    public RolesResponse roles(
            @Parameter(name = HttpHeaders.AUTHORIZATION, description = "Encabezado Authorization con esquema Bearer y el JWT", in = ParameterIn.HEADER, required = true)
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return authService.rolesDesdeAuthorization(authorization);
    }

    @Operation(
            summary = "Renovar JWT",
            description = "Con un Bearer válido (aunque lleve roles antiguos), obtiene roles vigentes en user-service y devuelve un token nuevo.")
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_BEARER)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token renovado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content)
    })
    @PostMapping("/refresh")
    public LoginResponse refresh(
            @Parameter(name = HttpHeaders.AUTHORIZATION, in = ParameterIn.HEADER, required = true)
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return authService.refreshDesdeAuthorization(authorization);
    }

    @Operation(
            summary = "Sincronizar rol vendedor",
            description =
                    "Con sesión válida: lee la solicitud ACTIVA por id, vuelve a aplicar promoción VENDEDOR en user-service "
                            + "según documento/correo de la solicitud y devuelve JWT con roles actualizados.")
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_BEARER)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token emitido con roles vigentes"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Solicitud no existe", content = @Content),
            @ApiResponse(responseCode = "409", description = "Solicitud no está ACTIVA", content = @Content),
            @ApiResponse(responseCode = "503", description = "solicitud-service no configurado", content = @Content)
    })
    @PostMapping("/sincronizar-vendedor")
    public LoginResponse sincronizarVendedor(
            @Parameter(name = HttpHeaders.AUTHORIZATION, in = ParameterIn.HEADER, required = true)
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid @RequestBody SincronizarVendedorRequest body) {
        return authService.sincronizarVendedorDesdeSolicitud(authorization, body);
    }
}
