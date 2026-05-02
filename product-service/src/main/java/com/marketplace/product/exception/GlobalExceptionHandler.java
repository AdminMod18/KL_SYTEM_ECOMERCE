package com.marketplace.product.exception;

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

/**
 * Propósito: centralizar errores de validación de requests del catálogo.
 * Patrón: manejo global de excepciones.
 * Responsabilidad: convertir fallos de Bean Validation a ProblemDetail 400.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validación de producto");
        pd.setType(URI.create("about:blank"));
        pd.setProperty("timestamp", Instant.now().toString());
        Map<String, String> campos = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            campos.put(fe.getField(), fe.getDefaultMessage());
        }
        pd.setProperty("campos", campos);
        return pd;
    }

    @ExceptionHandler(ProductoBusinessException.class)
    ProblemDetail handleNegocio(ProductoBusinessException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Regla de negocio");
        pd.setDetail(ex.getMessage());
        pd.setProperty("codigo", ex.getCodigo());
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

    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail handleIntegracion(IllegalStateException ex) {
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        if (ex.getMessage() != null && ex.getMessage().contains("Configure integracion.solicitud.base-url")) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        pd.setTitle("Integración solicitud-service");
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }
}
