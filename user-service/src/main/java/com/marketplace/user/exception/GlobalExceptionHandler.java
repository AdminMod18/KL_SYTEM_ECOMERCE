package com.marketplace.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Propósito: respuestas coherentes ante validación, conflictos y no encontrado.
 * Patrón: manejo global de excepciones.
 * Responsabilidad: mapear excepciones a ProblemDetail con HTTP adecuado.
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
        if (!campos.isEmpty()) {
            pd.setDetail(
                    campos.entrySet().stream()
                            .map(e -> e.getKey() + ": " + e.getValue())
                            .collect(Collectors.joining("; ")));
        }
        return pd;
    }

    @ExceptionHandler(UsuarioBusinessException.class)
    ProblemDetail handleBusiness(UsuarioBusinessException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Conflicto de usuario");
        pd.setProperty("codigo", ex.getCodigo());
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }

    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle(status.getReasonPhrase());
        pd.setDetail(ex.getReason());
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }
}
