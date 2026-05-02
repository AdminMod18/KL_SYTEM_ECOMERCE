package com.marketplace.validation.port;

import com.marketplace.validation.domain.ExigenciaJudicial;

/**
 * Consulta de validación judicial (implementación mock en desarrollo).
 */
public interface JudicialConsultaPort {

    ExigenciaJudicial consultarExigenciaPorDocumento(String documentoIdentidad);
}
