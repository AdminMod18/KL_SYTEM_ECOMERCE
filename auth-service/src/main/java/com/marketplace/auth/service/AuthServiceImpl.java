package com.marketplace.auth.service;

import com.marketplace.auth.dto.LoginRequest;
import com.marketplace.auth.dto.LoginResponse;
import com.marketplace.auth.dto.RolesResponse;
import com.marketplace.auth.exception.AuthException;
import com.marketplace.auth.dto.SincronizarVendedorRequest;
import com.marketplace.auth.integration.RegisteredUserLoginBridge;
import com.marketplace.auth.integration.SolicitudConsultaClient;
import com.marketplace.auth.integration.UserServicePromoverClient;
import com.marketplace.auth.integration.UserServiceRolesClient;
import com.marketplace.auth.security.JwtTokenProvider;
import com.marketplace.auth.user.CuentaUsuario;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Propósito: validar credenciales demo y emitir JWT con roles; validar JWT para consulta de roles.
 * Patrón: Real Subject detrás del Proxy (misma interfaz {@link AuthService}).
 * Responsabilidad: lógica de negocio de autenticación sin aspectos transversales de logging.
 */
@Service("authServiceReal")
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final Map<String, CuentaUsuario> usuariosDemo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RegisteredUserLoginBridge registeredUserLoginBridge;
    private final UserServiceRolesClient userServiceRolesClient;
    private final SolicitudConsultaClient solicitudConsultaClient;
    private final UserServicePromoverClient userServicePromoverClient;
    private final long expirationMs;

    public AuthServiceImpl(
            Map<String, CuentaUsuario> usuariosDemo,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RegisteredUserLoginBridge registeredUserLoginBridge,
            UserServiceRolesClient userServiceRolesClient,
            SolicitudConsultaClient solicitudConsultaClient,
            UserServicePromoverClient userServicePromoverClient,
            @Value("${auth.jwt.expiration-ms}") long expirationMs) {
        this.usuariosDemo = usuariosDemo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.registeredUserLoginBridge = registeredUserLoginBridge;
        this.userServiceRolesClient = userServiceRolesClient;
        this.solicitudConsultaClient = solicitudConsultaClient;
        this.userServicePromoverClient = userServicePromoverClient;
        this.expirationMs = expirationMs;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String user = request.getUsername().trim();
        String password = request.getPassword();
        CuentaUsuario cuentaDemo = usuariosDemo.get(user);
        if (cuentaDemo != null) {
            if (!passwordEncoder.matches(password, cuentaDemo.passwordHash())) {
                throw new AuthException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas.");
            }
            return construirRespuestaLogin(user, cuentaDemo.roles());
        }
        return registeredUserLoginBridge
                .tryLogin(user, password)
                .map(r -> construirRespuestaLogin(r.jwtSubject(), r.roles()))
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas."));
    }

    private LoginResponse construirRespuestaLogin(String subject, List<String> roles) {
        String token = jwtTokenProvider.crearToken(subject, roles);
        long expSegundos = Math.max(1L, expirationMs / 1000L);
        log.info("Login correcto usuario={} roles={}", subject, roles);
        return new LoginResponse(token, "Bearer", expSegundos, roles);
    }

    @Override
    public RolesResponse rolesDesdeAuthorization(String authorizationHeader) {
        String token = extraerBearerToken(authorizationHeader);
        String subject = jwtTokenProvider.extraerSubject(token);
        List<String> roles = jwtTokenProvider.extraerRoles(token);
        return new RolesResponse(subject, roles);
    }

    @Override
    public LoginResponse refreshDesdeAuthorization(String authorizationHeader) {
        String token = extraerBearerToken(authorizationHeader);
        try {
            String subject = jwtTokenProvider.extraerSubject(token);
            List<String> roles = resolverRolesActuales(subject);
            return construirRespuestaLogin(subject, roles);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado.");
        }
    }

    @Override
    public LoginResponse sincronizarVendedorDesdeSolicitud(
            String authorizationHeader, SincronizarVendedorRequest body) {
        String token = extraerBearerToken(authorizationHeader);
        String subject;
        try {
            subject = jwtTokenProvider.extraerSubject(token);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado.");
        }
        if (!solicitudConsultaClient.estaConfigurado()) {
            throw new AuthException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Integración solicitud-service no configurada (integration.solicitud.base-url).");
        }
        var solicitudOpt = solicitudConsultaClient.obtener(body.getSolicitudId());
        if (solicitudOpt.isEmpty()) {
            throw new AuthException(HttpStatus.NOT_FOUND, "Solicitud no encontrada.");
        }
        var sol = solicitudOpt.get();
        if (sol.estado() == null || !"ACTIVA".equalsIgnoreCase(sol.estado().trim())) {
            throw new AuthException(
                    HttpStatus.CONFLICT,
                    "La solicitud debe estar en estado ACTIVA para sincronizar el rol vendedor.");
        }
        userServicePromoverClient
                .promoverVendedor(sol.documentoIdentidad(), sol.correoElectronico())
                .ifPresentOrElse(
                        r -> log.info(
                                "Sincronizar vendedor: aplicado={} codigo={} usuarioPromovido={}",
                                r.aplicado(),
                                r.codigoRazon(),
                                r.nombreUsuario()),
                        () -> log.warn(
                                "Sincronizar vendedor: user-service no configurado o cuerpo vacío (documento/correo)."));

        List<String> roles = resolverRolesActuales(subject);
        return construirRespuestaLogin(subject, roles);
    }

    private List<String> resolverRolesActuales(String subject) {
        CuentaUsuario cuentaDemo = usuariosDemo.get(subject);
        if (cuentaDemo != null) {
            return cuentaDemo.roles();
        }
        Optional<List<String>> desdeUserService = userServiceRolesClient.fetchRolesByNombreUsuario(subject);
        return desdeUserService.orElse(List.of("COMPRADOR", "USER"));
    }

    private static String extraerBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Se requiere encabezado Authorization: Bearer <token>.");
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token vacío.");
        }
        return token;
    }
}
