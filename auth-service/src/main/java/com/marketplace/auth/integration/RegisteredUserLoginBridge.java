package com.marketplace.auth.integration;

import java.util.List;
import java.util.Optional;

/**
 * Intenta autenticar contra usuarios registrados en user-service (contraseña persistida allí).
 */
public interface RegisteredUserLoginBridge {

    record Result(String jwtSubject, List<String> roles) {}

    Optional<Result> tryLogin(String usernameOrEmail, String rawPassword);
}
