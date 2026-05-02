package com.marketplace.solicitud.facade;

import com.marketplace.solicitud.dto.SolicitudFiltrosConsulta;
import com.marketplace.solicitud.dto.SolicitudListadoItemResponse;
import com.marketplace.solicitud.entity.Solicitud;
import com.marketplace.solicitud.repository.SolicitudFiltrosSpecification;
import com.marketplace.solicitud.repository.SolicitudRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Propósito: caso de uso de consulta de solicitudes para supervisión (director).
 * Patrón: Facade — API estable sobre repositorio + especificaciones + mapeo a DTO de tabla.
 * Responsabilidad: aislar el armado del listado filtrado del resto del ciclo de vida de solicitudes.
 */
@Component
public class ConsultaSolicitudesDirectorFacade {

    private final SolicitudRepository solicitudRepository;

    public ConsultaSolicitudesDirectorFacade(SolicitudRepository solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    /**
     * Listado ordenado por fecha de creación descendente (más recientes primero).
     */
    public List<SolicitudListadoItemResponse> listarFiltrado(SolicitudFiltrosConsulta filtros) {
        SolicitudFiltrosConsulta f = filtros == null ? SolicitudFiltrosConsulta.sinFiltros() : filtros;
        Specification<Solicitud> spec = SolicitudFiltrosSpecification.construir(f);
        Sort orden = Sort.by(Sort.Direction.DESC, "creadoEn");
        return solicitudRepository.findAll(spec, orden).stream().map(this::aItemTabla).toList();
    }

    private SolicitudListadoItemResponse aItemTabla(Solicitud s) {
        return new SolicitudListadoItemResponse(
                s.getId(),
                s.getNumeroRadicado(),
                s.getNombreVendedor(),
                s.getNombres(),
                s.getApellidos(),
                s.getDocumentoIdentidad(),
                s.getCorreoElectronico(),
                s.getPaisResidencia(),
                s.getCiudadResidencia(),
                s.getTelefono(),
                s.getTipoPersona(),
                s.getEstado(),
                s.getCreadoEn(),
                s.getProximoVencimientoSuscripcion());
    }
}
