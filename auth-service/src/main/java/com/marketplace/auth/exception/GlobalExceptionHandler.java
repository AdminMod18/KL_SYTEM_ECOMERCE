package com.marketplace.auth.exception;

import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Propósito: homogeneizar errores de validación, credenciales y JWT.
 * Patrón: manejo global de excepciones.
 * Responsabilidad: traducir excepciones a ProblemDetail con códigos HTTP adecuados.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validación");
        pd.setType(URI.create("about:blank"));
        pd.setProperty("timestamp", Instant.now().toString());
        Map<String, String> campos = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            campos.put(fe.getField(), fe.getDefaultMessage());
        }
        pd.setProperty("campos", campos);
        return pd;
    }

    @ExceptionHandler(AuthException.class)
    ProblemDetail handleAuth(AuthException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(ex.getStatus());
        pd.setTitle("Autenticación");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }

    @ExceptionHandler(JwtException.class)
    ProblemDetail handleJwt(JwtException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("Token inválido");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }
}
