package com.marketplace.admin.dto;

import java.time.Instant;

public record AuditoriaItemResponse(
        Instant ocurridoEn, String actor, String accion, String detalle) {}
