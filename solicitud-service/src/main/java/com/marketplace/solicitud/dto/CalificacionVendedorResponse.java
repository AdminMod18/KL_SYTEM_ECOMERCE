package com.marketplace.solicitud.dto;

import java.time.Instant;

public record CalificacionVendedorResponse(
        Long id, Long solicitudId, int valor, String comentario, String referenciaOrden, Instant creadoEn) {}
