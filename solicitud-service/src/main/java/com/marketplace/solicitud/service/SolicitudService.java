package com.marketplace.solicitud.service;

import com.marketplace.solicitud.chain.SolicitudValidationHandler;
import com.marketplace.solicitud.dto.EstadoUpdateRequest;
import com.marketplace.solicitud.dto.SolicitudCreateRequest;
import com.marketplace.solicitud.dto.SolicitudResponse;
import com.marketplace.solicitud.entity.Solicitud;
import com.marketplace.solicitud.model.SolicitudEstado;
import com.marketplace.solicitud.repository.SolicitudRepository;
import com.marketplace.solicitud.state.SolicitudEstadoRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Propósito: casos de uso de solicitudes (crear, listar, cambiar estado).
 * Patrón: Application Service (capa de aplicación); orquesta Chain of Responsibility y State.
 * Responsabilidad: transacciones, mapeo a DTOs y delegación en cadena de validación y registro de estados.
 */
@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final SolicitudValidationHandler cadenaValidacionCreacion;
    private final SolicitudEstadoRegistry estadoRegistry;

    public SolicitudService(
            SolicitudRepository solicitudRepository,
            SolicitudValidationHandler cadenaValidacionCreacion,
            SolicitudEstadoRegistry estadoRegistry) {
        this.solicitudRepository = solicitudRepository;
        this.cadenaValidacionCreacion = cadenaValidacionCreacion;
        this.estadoRegistry = estadoRegistry;
    }

    @Transactional
    public SolicitudResponse crear(SolicitudCreateRequest request) {
        normalizar(request);
        cadenaValidacionCreacion.validar(request);

        Solicitud solicitud = new Solicitud();
        solicitud.setNombreVendedor(request.getNombreVendedor());
        solicitud.setDocumentoIdentidad(request.getDocumentoIdentidad());
        solicitud.setEstado(SolicitudEstado.PENDIENTE);

        return aRespuesta(solicitudRepository.save(solicitud));
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> listar() {
        return solicitudRepository.findAll().stream().map(this::aRespuesta).toList();
    }

    @Transactional
    public SolicitudResponse cambiarEstado(Long id, EstadoUpdateRequest request) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        estadoRegistry.comportamientoPara(solicitud.getEstado())
                .assertTransicionPermitida(request.getEstado());

        solicitud.setEstado(request.getEstado());
        return aRespuesta(solicitudRepository.save(solicitud));
    }

    private static void normalizar(SolicitudCreateRequest request) {
        request.setNombreVendedor(request.getNombreVendedor().trim());
        request.setDocumentoIdentidad(request.getDocumentoIdentidad().trim());
    }

    private SolicitudResponse aRespuesta(Solicitud solicitud) {
        return new SolicitudResponse(
                solicitud.getId(),
                solicitud.getNombreVendedor(),
                solicitud.getDocumentoIdentidad(),
                solicitud.getEstado(),
                solicitud.getCreadoEn());
    }
}
