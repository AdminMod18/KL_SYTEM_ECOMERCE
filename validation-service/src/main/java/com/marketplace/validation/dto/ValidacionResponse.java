package com.marketplace.validation.dto;

import com.marketplace.validation.domain.EstadoResultadoValidacionVendedor;
import com.marketplace.validation.domain.ExigenciaJudicial;
import com.marketplace.validation.domain.NivelClasificacionProveedor;

import java.util.List;

/**
 * Respuesta agregada: fuentes crudas, clasificación de negocio y estado sugerido para la solicitud de vendedor.
 */
public record ValidacionResponse(
        boolean apto,
        int scoreDatacredito,
        boolean listaControlDatacredito,
        String referenciaConsultaDatacredito,
        int indicadorRiesgoCifin,
        String estadoLineaCifin,
        boolean lineaCifinEncontrada,
        NivelClasificacionProveedor clasificacionDatacredito,
        NivelClasificacionProveedor clasificacionCifin,
        ExigenciaJudicial exigenciaJudicial,
        EstadoResultadoValidacionVendedor estadoVendedor,
        List<String> observaciones
) {
}
