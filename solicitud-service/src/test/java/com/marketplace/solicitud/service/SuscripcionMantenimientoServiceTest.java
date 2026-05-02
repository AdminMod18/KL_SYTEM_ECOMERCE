package com.marketplace.solicitud.service;

import com.marketplace.solicitud.entity.Solicitud;
import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.notification.SolicitudEstadoNotificador;
import com.marketplace.solicitud.repository.CalificacionVendedorRepository;
import com.marketplace.solicitud.repository.SolicitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SuscripcionMantenimientoServiceTest {

    @Autowired
    private SuscripcionMantenimientoService mantenimientoService;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private CalificacionVendedorRepository calificacionVendedorRepository;

    @Autowired
    private Clock clock;

    @MockBean
    private SolicitudEstadoNotificador solicitudEstadoNotificador;

    @BeforeEach
    void limpiar() {
        calificacionVendedorRepository.deleteAll();
        solicitudRepository.deleteAll();
    }

    @Test
    @DisplayName("ACTIVA con vencimiento en el pasado → EN_MORA")
    void activaVencida_marcadaEnMora() {
        Solicitud s = solicitudActivaVencida();
        mantenimientoService.ejecutarCiclo();
        assertThat(solicitudRepository.findById(s.getId()).orElseThrow().getEstado())
                .isEqualTo(SolicitudEstado.EN_MORA);
    }

    @Test
    @DisplayName("EN_MORA con gracia agotada → CANCELADA")
    void enMoraProlongada_cancelada() {
        Solicitud s = solicitudEnMoraGraciaAgotada();
        mantenimientoService.ejecutarCiclo();
        assertThat(solicitudRepository.findById(s.getId()).orElseThrow().getEstado())
                .isEqualTo(SolicitudEstado.CANCELADA);
    }

    private Solicitud solicitudActivaVencida() {
        Solicitud s = new Solicitud();
        s.setNombreVendedor("Vendedor Test");
        s.setDocumentoIdentidad(UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        s.setCorreoElectronico("v@test.com");
        s.setEstado(SolicitudEstado.ACTIVA);
        s.setProximoVencimientoSuscripcion(clock.instant().minus(2, ChronoUnit.DAYS));
        return solicitudRepository.save(s);
    }

    private Solicitud solicitudEnMoraGraciaAgotada() {
        Solicitud s = new Solicitud();
        s.setNombreVendedor("Vendedor Mora");
        s.setDocumentoIdentidad(UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        s.setCorreoElectronico("m@test.com");
        s.setEstado(SolicitudEstado.EN_MORA);
        Instant ahora = clock.instant();
        s.setProximoVencimientoSuscripcion(ahora.minus(60, ChronoUnit.DAYS));
        s.setEntradaEnMoraEn(ahora.minus(40, ChronoUnit.DAYS));
        return solicitudRepository.save(s);
    }
}
