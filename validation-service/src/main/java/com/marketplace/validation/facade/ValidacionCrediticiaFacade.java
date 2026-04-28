package com.marketplace.validation.facade;

import com.marketplace.validation.domain.ResultadoCifin;
import com.marketplace.validation.domain.ResultadoDatacredito;
import com.marketplace.validation.dto.ValidacionRequest;
import com.marketplace.validation.dto.ValidacionResponse;
import com.marketplace.validation.port.CifinArchivoPort;
import com.marketplace.validation.port.DatacreditoConsultaPort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Propósito: exponer una única operación de alto nivel para validar un solicitante ante Datacrédito y CIFIN.
 * Patrón: Facade (oculta la interacción entre subsistemas y reglas de decisión agregadas).
 * Responsabilidad: invocar puertos, aplicar políticas de aprobación y devolver un DTO unificado al controlador.
 */
@Service
public class ValidacionCrediticiaFacade {

    private static final int SCORE_DATACREDITO_MINIMO = 550;
    private static final int RIESGO_CIFIN_MAXIMO = 750;

    private final DatacreditoConsultaPort datacreditoConsultaPort;
    private final CifinArchivoPort cifinArchivoPort;

    public ValidacionCrediticiaFacade(
            DatacreditoConsultaPort datacreditoConsultaPort,
            CifinArchivoPort cifinArchivoPort) {
        this.datacreditoConsultaPort = datacreditoConsultaPort;
        this.cifinArchivoPort = cifinArchivoPort;
    }

    public ValidacionResponse validar(ValidacionRequest request) {
        String documento = request.getDocumentoIdentidad().trim();

        ResultadoDatacredito datacredito = datacreditoConsultaPort.consultarPorDocumento(documento);
        ResultadoCifin cifin = cifinArchivoPort.interpretarParaDocumento(
                request.getContenidoArchivoCifin(),
                documento);

        List<String> observaciones = new ArrayList<>();

        if (datacredito.listaControl()) {
            observaciones.add("Documento reportado en lista de control Datacrédito.");
        }
        if (datacredito.score() < SCORE_DATACREDITO_MINIMO) {
            observaciones.add("Score Datacrédito inferior al umbral permitido.");
        }
        if (!cifin.informacionEncontrada()) {
            observaciones.add("No hay línea CIFIN para el documento indicado.");
        } else if (cifin.indicadorRiesgo() > RIESGO_CIFIN_MAXIMO) {
            observaciones.add("Indicador de riesgo CIFIN por encima del máximo permitido.");
        } else if (!"NORMAL".equalsIgnoreCase(cifin.estadoLinea())) {
            observaciones.add("Estado CIFIN no favorable: " + cifin.estadoLinea());
        }

        boolean apto = observaciones.isEmpty();

        return new ValidacionResponse(
                apto,
                datacredito.score(),
                datacredito.listaControl(),
                datacredito.referenciaConsulta(),
                cifin.indicadorRiesgo(),
                cifin.estadoLinea(),
                cifin.informacionEncontrada(),
                observaciones);
    }
}
