package com.marketplace.solicitud.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Cuerpo para ejecutar validacion automatica (CIFIN simulado como archivo plano).
 * <p>O bien {@code contenidoArchivoCifin}, o bien {@code documento} + {@code score} que debe coincidir
 * con la solicitud y construye una linea {@code DOCUMENTO|score|NORMAL} para el adaptador CIFIN.
 * <p><strong>No usar {@code @NotBlank} ni {@code @NotEmpty} en {@code contenidoArchivoCifin}:</strong> el modo
 * solo {@code documento}+{@code score} es válido y debe pasar la validación Bean antes de llegar al servicio.
 */
@Getter
@Setter
public class ValidacionAutomaticaRequest {

    /**
     * Lineas estilo batch CIFIN; opcional si envia documento + score.
     */
    @Size(max = 50_000)
    private String contenidoArchivoCifin;

    /**
     * Debe coincidir con {@code documentoIdentidad} de la solicitud (si no envia contenidoArchivoCifin).
     */
    @Size(max = 32)
    private String documento;

    /**
     * Indicador de riesgo simulado en la linea CIFIN (tipico 400-599 con estado NORMAL para politica ALTA).
     */
    @Min(0)
    @Max(9999)
    private Integer score;

    /**
     * Consulta antecedentes policía registrada por el Director (HU-07). Si no se envía, se usa el mock por documento.
     */
    @Size(max = 16)
    private String exigenciaJudicial;
}
