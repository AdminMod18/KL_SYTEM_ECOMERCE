package com.marketplace.validation.facade;

import com.marketplace.validation.domain.EstadoResultadoValidacionVendedor;
import com.marketplace.validation.domain.ExigenciaJudicial;
import com.marketplace.validation.domain.NivelClasificacionProveedor;
import com.marketplace.validation.domain.ResultadoCifin;
import com.marketplace.validation.domain.ResultadoDatacredito;
import com.marketplace.validation.dto.ValidacionRequest;
import com.marketplace.validation.dto.ValidacionResponse;
import com.marketplace.validation.policy.ClasificadorNivelCifin;
import com.marketplace.validation.policy.ClasificadorNivelDatacredito;
import com.marketplace.validation.policy.PoliticaEstadoVendedor;
import com.marketplace.validation.port.CifinArchivoPort;
import com.marketplace.validation.port.DatacreditoConsultaPort;
import com.marketplace.validation.port.JudicialConsultaPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquesta Datacrédito (REST mock), CIFIN (archivo plano) y validación judicial (mock), y aplica la política de vendedor.
 */
@Service
public class ValidacionCrediticiaFacade {

    private static final Logger log = LoggerFactory.getLogger(ValidacionCrediticiaFacade.class);

    private final DatacreditoConsultaPort datacreditoConsultaPort;
    private final CifinArchivoPort cifinArchivoPort;
    private final JudicialConsultaPort judicialConsultaPort;

    public ValidacionCrediticiaFacade(
            DatacreditoConsultaPort datacreditoConsultaPort,
            CifinArchivoPort cifinArchivoPort,
            JudicialConsultaPort judicialConsultaPort) {
        this.datacreditoConsultaPort = datacreditoConsultaPort;
        this.cifinArchivoPort = cifinArchivoPort;
        this.judicialConsultaPort = judicialConsultaPort;
    }

    public ValidacionResponse validar(ValidacionRequest request) {
        String documento = request.getDocumentoIdentidad().trim();
        log.info("Validación recibida documento={} solicitante={}", documento, request.getNombreSolicitante());

        ResultadoDatacredito datacredito = datacreditoConsultaPort.consultarPorDocumento(documento);
        ResultadoCifin cifin = cifinArchivoPort.interpretarParaDocumento(
                request.getContenidoArchivoCifin(),
                documento);
        ExigenciaJudicial judicial =
                request.getExigenciaJudicialDirector() != null
                        ? request.getExigenciaJudicialDirector()
                        : judicialConsultaPort.consultarExigenciaPorDocumento(documento);

        log.info("Score Datacrédito recibido: {}", datacredito.score());
        log.info(
                "Indicador riesgo CIFIN (score línea): {} estadoLinea={} informacionEncontrada={}",
                cifin.indicadorRiesgo(),
                cifin.estadoLinea(),
                cifin.informacionEncontrada());

        NivelClasificacionProveedor nivelDc = ClasificadorNivelDatacredito.clasificar(datacredito);
        NivelClasificacionProveedor nivelCifin = ClasificadorNivelCifin.clasificar(cifin);

        PoliticaEstadoVendedor.ResultadoPolitica politica =
                PoliticaEstadoVendedor.evaluar(nivelDc, nivelCifin, judicial);

        List<String> observaciones = new ArrayList<>(politica.observacionesPolitica());
        if (datacredito.listaControl()) {
            observaciones.add(0, "Documento reportado en lista de control Datacrédito.");
        }

        EstadoResultadoValidacionVendedor estado = politica.estado();
        boolean apto = estado == EstadoResultadoValidacionVendedor.APROBADA;

        log.info("Estado vendedor calculado: {}", estado);
        log.info(
                "Validación agregada documento={} estadoVendedor={} niveles(dc={}, cifin={}) judicial={}",
                documento,
                estado,
                nivelDc,
                nivelCifin,
                judicial);

        return new ValidacionResponse(
                apto,
                datacredito.score(),
                datacredito.listaControl(),
                datacredito.referenciaConsulta(),
                cifin.indicadorRiesgo(),
                cifin.estadoLinea(),
                cifin.informacionEncontrada(),
                nivelDc,
                nivelCifin,
                judicial,
                estado,
                List.copyOf(observaciones));
    }
}
