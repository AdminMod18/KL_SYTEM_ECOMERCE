package com.marketplace.validation.dto;

import com.marketplace.validation.domain.ExigenciaJudicial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Propósito: entrada HTTP para validación combinada Datacrédito + CIFIN.
 * Patrón: DTO (capa API).
 * Responsabilidad: transportar documento, nombre y contenido plano del archivo CIFIN sin exponer integraciones.
 */
@Getter
@Setter
public class ValidacionRequest {

    @NotBlank
    @Size(max = 32)
    private String documentoIdentidad;

    @NotBlank
    @Size(max = 200)
    private String nombreSolicitante;

    /**
     * Contenido íntegro del archivo plano CIFIN (simulación batch).
     */
    @NotBlank
    private String contenidoArchivoCifin;

    /**
     * Si el Director Comercial registra consulta de antecedentes (HU-07), sustituye el mock judicial.
     */
    private ExigenciaJudicial exigenciaJudicialDirector;
}
