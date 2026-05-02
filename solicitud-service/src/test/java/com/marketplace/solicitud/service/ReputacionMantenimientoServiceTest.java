package com.marketplace.solicitud.service;

import com.marketplace.solicitud.entity.CalificacionVendedor;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReputacionMantenimientoServiceTest {

    @Autowired
    private ReputacionMantenimientoService reputacionMantenimientoService;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private CalificacionVendedorRepository calificacionVendedorRepository;

    @MockBean
    private SolicitudEstadoNotificador solicitudEstadoNotificador;

    @BeforeEach
    void limpiar() {
        calificacionVendedorRepository.deleteAll();
        solicitudRepository.deleteAll();
    }

    @Test
    @DisplayName("10 calificaciones con nota 2 (<3) → CANCELADA")
    void diezMalas_cancelada() {
        Solicitud s = solicitudActiva();
        for (int i = 0; i < 10; i++) {
            guardarCalificacion(s, 2);
        }
        reputacionMantenimientoService.ejecutarCiclo();
        assertThat(solicitudRepository.findById(s.getId()).orElseThrow().getEstado())
                .isEqualTo(SolicitudEstado.CANCELADA);
    }

    @Test
    @DisplayName("Promedio < 5 con pocas calificaciones → CANCELADA")
    void promedioBajo_cancelada() {
        Solicitud s = solicitudActiva();
        guardarCalificacion(s, 4);
        guardarCalificacion(s, 4);
        guardarCalificacion(s, 4);
        reputacionMantenimientoService.ejecutarCiclo();
        assertThat(solicitudRepository.findById(s.getId()).orElseThrow().getEstado())
                .isEqualTo(SolicitudEstado.CANCELADA);
    }

    @Test
    @DisplayName("9 malas y promedio alto → sigue ACTIVA")
    void sinUmbralMalas_niPromedioCritico_seMantiene() {
        Solicitud s = solicitudActiva();
        for (int i = 0; i < 9; i++) {
            guardarCalificacion(s, 2);
        }
        for (int i = 0; i < 6; i++) {
            guardarCalificacion(s, 10);
        }
        reputacionMantenimientoService.ejecutarCiclo();
        assertThat(solicitudRepository.findById(s.getId()).orElseThrow().getEstado())
                .isEqualTo(SolicitudEstado.ACTIVA);
    }

    private Solicitud solicitudActiva() {
        Solicitud s = new Solicitud();
        s.setNombreVendedor("Vendedor Rep");
        s.setDocumentoIdentidad(UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        s.setCorreoElectronico("rep@test.com");
        s.setEstado(SolicitudEstado.ACTIVA);
        return solicitudRepository.save(s);
    }

    private void guardarCalificacion(Solicitud solicitud, int valor) {
        CalificacionVendedor c = new CalificacionVendedor();
        c.setSolicitud(solicitud);
        c.setValor(valor);
        calificacionVendedorRepository.save(c);
    }
}
