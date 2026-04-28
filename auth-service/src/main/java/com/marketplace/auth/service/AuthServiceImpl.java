package com.marketplace.auth.service;

import com.marketplace.auth.dto.LoginRequest;
import com.marketplace.auth.dto.LoginResponse;
import com.marketplace.auth.dto.RolesResponse;
import com.marketplace.auth.exception.AuthException;
import com.marketplace.auth.security.JwtTokenProvider;
import com.marketplace.auth.user.CuentaUsuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Propósito: validar credenciales demo y emitir JWT con roles; validar JWT para consulta de roles.
 * Patrón: Real Subject detrás del Proxy (misma interfaz {@link AuthService}).
 * Responsabilidad: lógica de negocio de autenticación sin aspectos transversales de logging.
 */
@Service("authServiceReal")
public class AuthServiceImpl implements AuthService {

    private final Map<String, CuentaUsuario> usuariosDemo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final long expirationMs;

    public AuthServiceImpl(
            Map<String, CuentaUsuario> usuariosDemo,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            @Value("${auth.jwt.expiration-ms}") long expirationMs) {
        this.usuariosDemo = usuariosDemo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.expirationMs = expirationMs;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String user = request.getUsername().trim();
        CuentaUsuario cuenta = usuariosDemo.get(user);
        if (cuenta == null || !passwordEncoder.matches(request.getPassword(), cuenta.passwordHash())) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas.");
        }
        String token = jwtTokenProvider.crearToken(user, cuenta.roles());
        long expSegundos = Math.max(1L, expirationMs / 1000L);
        return new LoginResponse(token, "Bearer", expSegundos, cuenta.roles());
    }

    @Override
    public RolesResponse rolesDesdeAuthorization(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Se requiere encabezado Authorization: Bearer <token>.");
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token vacío.");
        }
        String subject = jwtTokenProvider.extraerSubject(token);
        List<String> roles = jwtTokenProvider.extraerRoles(token);
        return new RolesResponse(subject, roles);
    }
}
