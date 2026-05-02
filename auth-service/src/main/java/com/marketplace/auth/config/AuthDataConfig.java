package com.marketplace.auth.config;

import com.marketplace.auth.user.CuentaUsuario;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Propósito: registrar usuarios demo en memoria para login y emisión de JWT.
 * Patrón: configuración Spring (Composition Root de datos de prueba).
 * Responsabilidad: exponer {@link PasswordEncoder} y mapa usuario → {@link CuentaUsuario}.
 */
@Configuration
public class AuthDataConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Map<String, CuentaUsuario> usuariosDemo(PasswordEncoder passwordEncoder) {
        Map<String, CuentaUsuario> mapa = new HashMap<>();
        mapa.put("admin", new CuentaUsuario(passwordEncoder.encode("admin123"), List.of("ADMIN", "USER")));
        mapa.put(
                "vendedor",
                new CuentaUsuario(
                        passwordEncoder.encode("vendedor123"),
                        List.of("VENDEDOR", "COMPRADOR", "USER")));
        return Map.copyOf(mapa);
    }
}
