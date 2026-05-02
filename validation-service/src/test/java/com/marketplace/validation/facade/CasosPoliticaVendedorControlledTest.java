package com.marketplace.validation.facade;

import com.marketplace.validation.domain.EstadoResultadoValidacionVendedor;
import com.marketplace.validation.domain.ExigenciaJudicial;
import com.marketplace.validation.domain.ResultadoCifin;
import com.marketplace.validation.domain.ResultadoDatacredito;
import com.marketplace.validation.dto.ValidacionRequest;
import com.marketplace.validation.dto.ValidacionResponse;
import com.marketplace.validation.port.CifinArchivoPort;
import com.marketplace.validation.port.DatacreditoConsultaPort;
import com.marketplace.validation.port.JudicialConsultaPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Pruebas controladas: respuestas simuladas de proveedores, ejecución completa de la fachada
 * y aserción estado esperado vs {@link ValidacionResponse#estadoVendedor()}.
 */
@ExtendWith(MockitoExtension.class)
class CasosPoliticaVendedorControlledTest {

    @Mock
    private DatacreditoConsultaPort datacreditoConsultaPort;

    @Mock
    private CifinArchivoPort cifinArchivoPort;

    @Mock
    private JudicialConsultaPort judicialConsultaPort;

    private ValidacionCrediticiaFacade facade;

    @BeforeEach
    void setUp() {
        facade = new ValidacionCrediticiaFacade(datacreditoConsultaPort, cifinArchivoPort, judicialConsultaPort);
    }

    @Test
    @DisplayName("Caso 1: Datacrédito BAJA + CIFIN ALTA → RECHAZADA")
    void caso1_datacreditoBaja_cifinAlta_rechazada() {
        when(datacreditoConsultaPort.consultarPorDocumento(anyString()))
                .thenReturn(new ResultadoDatacredito(300, false, "SIM-DC-1"));
        when(cifinArchivoPort.interpretarParaDocumento(anyString(), anyString()))
                .thenReturn(new ResultadoCifin(700, "NORMAL", true));
        when(judicialConsultaPort.consultarExigenciaPorDocumento(anyString()))
                .thenReturn(ExigenciaJudicial.NO_REQUERIDO);

        ValidacionResponse r = facade.validar(request("CC-CASO-1"));

        assertThat(r.clasificacionDatacredito().name()).isEqualTo("BAJA");
        assertThat(r.clasificacionCifin().name()).isEqualTo("ALTA");
        assertThat(r.estadoVendedor()).isEqualTo(EstadoResultadoValidacionVendedor.RECHAZADA);
        assertThat(r.apto()).isFalse();
    }

    @Test
    @DisplayName("Caso 2: Datacrédito ADVERTENCIA + CIFIN ALTA → DEVUELTA")
    void caso2_datacreditoAdvertencia_cifinAlta_devuelta() {
        when(datacreditoConsultaPort.consultarPorDocumento(anyString()))
                .thenReturn(new ResultadoDatacredito(620, false, "SIM-DC-2"));
        when(cifinArchivoPort.interpretarParaDocumento(anyString(), anyString()))
                .thenReturn(new ResultadoCifin(700, "NORMAL", true));
        when(judicialConsultaPort.consultarExigenciaPorDocumento(anyString()))
                .thenReturn(ExigenciaJudicial.NO_REQUERIDO);

        ValidacionResponse r = facade.validar(request("CC-CASO-2"));

        assertThat(r.clasificacionDatacredito().name()).isEqualTo("ADVERTENCIA");
        assertThat(r.clasificacionCifin().name()).isEqualTo("ALTA");
        assertThat(r.estadoVendedor()).isEqualTo(EstadoResultadoValidacionVendedor.DEVUELTA);
        assertThat(r.apto()).isFalse();
    }

    @Test
    @DisplayName("Caso 3: Datacrédito ALTA + CIFIN ALTA + judicial NO_REQUERIDO → APROBADA")
    void caso3_ambasAlta_judicialNoRequerido_aprobada() {
        when(datacreditoConsultaPort.consultarPorDocumento(anyString()))
                .thenReturn(new ResultadoDatacredito(720, false, "SIM-DC-3"));
        when(cifinArchivoPort.interpretarParaDocumento(anyString(), anyString()))
                .thenReturn(new ResultadoCifin(700, "NORMAL", true));
        when(judicialConsultaPort.consultarExigenciaPorDocumento(anyString()))
                .thenReturn(ExigenciaJudicial.NO_REQUERIDO);

        ValidacionResponse r = facade.validar(request("CC-CASO-3"));

        assertThat(r.clasificacionDatacredito().name()).isEqualTo("ALTA");
        assertThat(r.clasificacionCifin().name()).isEqualTo("ALTA");
        assertThat(r.exigenciaJudicial()).isEqualTo(ExigenciaJudicial.NO_REQUERIDO);
        assertThat(r.estadoVendedor()).isEqualTo(EstadoResultadoValidacionVendedor.APROBADA);
        assertThat(r.apto()).isTrue();
    }

    private static ValidacionRequest request(String documento) {
        ValidacionRequest req = new ValidacionRequest();
        req.setDocumentoIdentidad(documento);
        req.setNombreSolicitante("Vendedor simulado");
        req.setContenidoArchivoCifin(documento + "|700|NORMAL");
        return req;
    }
}
