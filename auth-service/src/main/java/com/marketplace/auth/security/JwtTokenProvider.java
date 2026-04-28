package com.marketplace.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Propósito: crear y validar tokens JWT firmados (HS256) con claim de roles.
 * Patrón: componente de infraestructura de seguridad.
 * Responsabilidad: firmar tokens y extraer subject/roles de forma centralizada.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${auth.jwt.secret}") String secret,
            @Value("${auth.jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String crearToken(String username, List<String> roles) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expirationMs);
        return Jwts.builder()
                .subject(username)
                .claims(Map.of("roles", roles))
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(secretKey)
                .compact();
    }

    @SuppressWarnings("unchecked")
    public List<String> extraerRoles(String token) {
        Claims claims = parseClaims(token);
        Object raw = claims.get("roles");
        if (raw instanceof List<?> lista) {
            return lista.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    public String extraerSubject(String token) {
        return parseClaims(token).getSubject();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
