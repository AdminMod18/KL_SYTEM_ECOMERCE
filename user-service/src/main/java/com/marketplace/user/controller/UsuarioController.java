package com.marketplace.user.controller;

import com.marketplace.user.dto.UsuarioCreateRequest;
import com.marketplace.user.dto.UsuarioResponse;
import com.marketplace.user.dto.UsuarioUpdateRequest;
import com.marketplace.user.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Propósito: API REST CRUD de usuarios del marketplace.
 * Patrón: Adapter HTTP.
 * Responsabilidad: validar entrada y delegar en {@link UsuarioService}.
 */
@Tag(name = "Usuarios", description = "CRUD de usuarios del marketplace")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Crear usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del nuevo usuario", required = true)
            @Valid @RequestBody UsuarioCreateRequest request) {
        return usuarioService.crear(request);
    }

    @Operation(summary = "Listar usuarios")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista de usuarios")})
    @GetMapping
    public List<UsuarioResponse> listar(
            @Parameter(name = "X-Request-Id", description = "Identificador de correlación opcional", in = ParameterIn.HEADER, required = false)
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        return usuarioService.listar();
    }

    @Operation(summary = "Obtener usuario por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe", content = @Content)
    })
    @GetMapping("/{id}")
    public UsuarioResponse obtener(
            @Parameter(description = "Identificador del usuario", required = true) @PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @Operation(summary = "Actualizar usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe", content = @Content)
    })
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(
            @Parameter(description = "Identificador del usuario", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Campos a modificar", required = true)
            @Valid @RequestBody UsuarioUpdateRequest request) {
        return usuarioService.actualizar(id, request);
    }

    @Operation(summary = "Eliminar usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminado"),
            @ApiResponse(responseCode = "404", description = "No existe", content = @Content)
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @Parameter(description = "Identificador del usuario", required = true) @PathVariable Long id) {
        usuarioService.eliminar(id);
    }
}
