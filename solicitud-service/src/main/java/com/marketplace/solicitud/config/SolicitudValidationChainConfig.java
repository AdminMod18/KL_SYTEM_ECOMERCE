package com.marketplace.solicitud.config;

import com.marketplace.solicitud.chain.CamposObligatoriosHandler;
import com.marketplace.solicitud.chain.DocumentoIdentidadFormatoHandler;
import com.marketplace.solicitud.chain.DocumentoNoDuplicadoHandler;
import com.marketplace.solicitud.chain.SolicitudValidationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Propósito: ensamblar la cadena de validación para creación de solicitudes.
 * Patrón: Chain of Responsibility (ensamblaje explícito del orden de eslabones).
 * Responsabilidad: definir el primer eslabón y el orden sin acoplar handlers entre sí.
 */
@Configuration
public class SolicitudValidationChainConfig {

    @Bean
    @Primary
    public SolicitudValidationHandler solicitudValidationChain(
            CamposObligatoriosHandler camposObligatoriosHandler,
            DocumentoIdentidadFormatoHandler documentoIdentidadFormatoHandler,
            DocumentoNoDuplicadoHandler documentoNoDuplicadoHandler) {
        camposObligatoriosHandler.enlazar(documentoIdentidadFormatoHandler);
        documentoIdentidadFormatoHandler.enlazar(documentoNoDuplicadoHandler);
        return camposObligatoriosHandler;
    }
}
