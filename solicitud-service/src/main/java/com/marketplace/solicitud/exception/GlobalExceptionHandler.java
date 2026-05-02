package com.marketplace.solicitud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Propósito: respuestas HTTP coherentes ante errores de validación y negocio.
 * Patrón: Centralized Exception Handling (cross-cutting).
 * Responsabilidad: mapear excepciones a ProblemDetail y registrar contexto de auditoría mínimo.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validación");
        pd.setType(URI.create("about:blank"));
        pd.setProperty("timestamp", Instant.now().toString());
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        pd.setProperty("campos", fieldErrors);
        log.warn("Body JSON rechazado (@Valid): {}", fieldErrors);
        return pd;
    }

    @ExceptionHandler(SolicitudBusinessException.class)
    ProblemDetail handleBusiness(SolicitudBusinessException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Regla de negocio");
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

    /**
     * Errores de integración con validation-service (HTTP, timeout, respuesta inválida).
     */
    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail handleIntegracionValidacion(IllegalStateException ex) {
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        if (ex.getMessage() != null
                && (ex.getMessage().contains("Configure integracion.validation.base-url")
                        || ex.getMessage().contains("Configure integracion.payment.base-url"))) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        pd.setTitle("Integración externa");
        pd.setType(URI.create("about:blank"));
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }
}
