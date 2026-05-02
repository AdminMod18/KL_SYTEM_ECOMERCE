package com.marketplace.validation.adapter.judicial;

import com.marketplace.validation.domain.ExigenciaJudicial;
import com.marketplace.validation.port.JudicialConsultaPort;
import org.springframework.stereotype.Component;

/**
 * Mock judicial: si el documento contiene la marca {@code JUD} se exige revisión (no aprobación automática).
 */
@Component
public class JudicialMockAdapter implements JudicialConsultaPort {

    private static final String MARCA_REVISION_JUDICIAL = "JUD";

    @Override
    public ExigenciaJudicial consultarExigenciaPorDocumento(String documentoIdentidad) {
        if (documentoIdentidad == null || documentoIdentidad.isBlank()) {
            return ExigenciaJudicial.REQUERIDO;
        }
        String d = documentoIdentidad.toUpperCase();
        return d.contains(MARCA_REVISION_JUDICIAL) ? ExigenciaJudicial.REQUERIDO : ExigenciaJudicial.NO_REQUERIDO;
    }
}
