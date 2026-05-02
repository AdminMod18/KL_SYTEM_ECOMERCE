package com.marketplace.admin.dto;

import java.time.Instant;

public record ParametroResponse(String clave, String valor, Instant actualizadoEn) {}
