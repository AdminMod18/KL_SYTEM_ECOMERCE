package com.marketplace.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParametroUpdateRequest {

    @NotBlank
    @Size(max = 2000)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String valor;
}
