package com.marketplace.admin.dto;

import java.time.Instant;

public record LogErrorItemResponse(Instant ocurridoEn, String nivel, String mensaje, String origen) {}
