package com.marketplace.user.controller;

import com.marketplace.user.dto.interno.VerificarCredencialesRequest;
import com.marketplace.user.dto.interno.VerificarCredencialesResponse;
import com.marketplace.user.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/interno")
@Validated
public class InternoCredencialesController {

    private final UsuarioService usuarioService;
    private final String internalSecret;

    public InternoCredencialesController(
            UsuarioService usuarioService,
            @Value("${integration.interno.secret:}") String internalSecret) {
        this.usuarioService = usuarioService;
        this.internalSecret = internalSecret;
    }

    /**
     * Solo para auth-service en red interna. Protegido por secreto compartido.
     */
    @PostMapping("/verificar-credenciales")
    public ResponseEntity<VerificarCredencialesResponse> verificar(
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret,
            @Valid @RequestBody VerificarCredencialesRequest body) {
        if (internalSecret.isBlank() || secret == null || !internalSecret.equals(secret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return usuarioService
                .verificarCredencialesSiCoinciden(body.getUsernameOrEmail(), body.getPassword())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
