package com.marketplace.user.controller;

import com.marketplace.user.dto.interno.PromoverVendedorRequest;
import com.marketplace.user.dto.interno.PromoverVendedorResponse;
import com.marketplace.user.dto.interno.RolesListaResponse;
import com.marketplace.user.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/interno")
@Validated
public class InternoVendedorController {

    private final UsuarioService usuarioService;
    private final String internalSecret;

    public InternoVendedorController(
            UsuarioService usuarioService, @Value("${integration.interno.secret:}") String internalSecret) {
        this.usuarioService = usuarioService;
        this.internalSecret = internalSecret;
    }

    @PostMapping("/promover-vendedor")
    public ResponseEntity<PromoverVendedorResponse> promover(
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret,
            @Valid @RequestBody PromoverVendedorRequest body) {
        if (!secretOk(secret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        PromoverVendedorResponse out =
                usuarioService.promoverAVendedorPorDocumentoOCorreo(
                        body.getDocumentoIdentidad(), body.getCorreoElectronico());
        return ResponseEntity.ok(out);
    }

    @GetMapping("/usuarios/{nombreUsuario}/roles")
    public ResponseEntity<RolesListaResponse> roles(
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret,
            @PathVariable String nombreUsuario) {
        if (!secretOk(secret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return usuarioService
                .rolesPorNombreUsuario(nombreUsuario)
                .map(r -> ResponseEntity.ok(new RolesListaResponse(r)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    private boolean secretOk(String secret) {
        return !internalSecret.isBlank() && secret != null && internalSecret.equals(secret);
    }
}
