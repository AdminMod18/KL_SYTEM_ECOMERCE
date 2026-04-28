package com.marketplace.order.command;

import com.marketplace.order.dto.OrdenResponse;
import org.springframework.stereotype.Component;

/**
 * Propósito: punto único de entrada para ejecutar comandos de orden sin acoplar a HTTP.
 * Patrón: Invoker asociado al catálogo Command (GoF).
 * Responsabilidad: invocar {@link ComandoOrden#ejecutar()} permitiendo extender con cola, logging o transacciones.
 */
@Component
public class InvocadorComandosOrden {

    public OrdenResponse ejecutar(ComandoOrden comando) {
        return comando.ejecutar();
    }
}
