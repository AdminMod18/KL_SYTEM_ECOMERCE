package com.marketplace.auth.dto;

import jakarta.validation.constraints.NotNull;

public class SincronizarVendedorRequest {

    @NotNull
    private Long solicitudId;

    public Long getSolicitudId() {
        return solicitudId;
    }

    public void setSolicitudId(Long solicitudId) {
        this.solicitudId = solicitudId;
    }
}
