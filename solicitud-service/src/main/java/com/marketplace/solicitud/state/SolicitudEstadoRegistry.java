package com.marketplace.solicitud.state;

import com.marketplace.solicitud.exception.SolicitudBusinessException;
import com.marketplace.solicitud.model.SolicitudEstado;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Propósito: resolver el comportamiento de estado actual para validar transiciones.
 * Patrón: Registry + Singleton (una instancia por JVM gestionada por Spring IoC).
 * Responsabilidad: mapear cada {@link SolicitudEstado} a su {@link SolicitudEstadoBehavior} inyectado.
 */
@Component
public class SolicitudEstadoRegistry {

    private final Map<SolicitudEstado, SolicitudEstadoBehavior> porEstado;

    public SolicitudEstadoRegistry(List<SolicitudEstadoBehavior> comportamientos) {
        this.porEstado = comportamientos.stream()
                .collect(Collectors.toMap(SolicitudEstadoBehavior::estado, Function.identity()));
    }

    public SolicitudEstadoBehavior comportamientoPara(SolicitudEstado estado) {
        SolicitudEstadoBehavior behavior = porEstado.get(estado);
        if (behavior == null) {
            throw new SolicitudBusinessException("ESTADO_SIN_COMPORTAMIENTO",
                    "No hay comportamiento registrado para el estado " + estado);
        }
        return behavior;
    }
}
