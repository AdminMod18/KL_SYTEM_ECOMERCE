package com.marketplace.order.command;

import com.marketplace.order.dto.OrdenResponse;

/**
 * Propósito: encapsular una acción reversible o auditable sobre el dominio de órdenes.
 * Patrón: Command (interfaz de comando con método {@link #ejecutar()}).
 * Responsabilidad: desacoplar la solicitud HTTP del procedimiento concreto que muta el sistema.
 */
public interface ComandoOrden {

    OrdenResponse ejecutar();
}
