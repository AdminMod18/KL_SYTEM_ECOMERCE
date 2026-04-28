package com.marketplace.order.service;

import com.marketplace.order.command.ComandoColocarOrden;
import com.marketplace.order.command.ComandoOrden;
import com.marketplace.order.command.InvocadorComandosOrden;
import com.marketplace.order.dto.OrdenCreateRequest;
import com.marketplace.order.dto.OrdenResponse;
import com.marketplace.order.pricing.CalculadorPrecioOrden;
import com.marketplace.order.repository.OrdenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Propósito: capa de aplicación que construye comandos y los despacha al invocador.
 * Patrón: Application Service + Command client.
 * Responsabilidad: transaccionalidad y ensamblaje del {@link ComandoColocarOrden} con dependencias Spring.
 */
@Service
public class OrdenApplicationService {

    private final InvocadorComandosOrden invocadorComandosOrden;
    private final OrdenRepository ordenRepository;
    private final CalculadorPrecioOrden calculadorPrecioOrden;

    public OrdenApplicationService(
            InvocadorComandosOrden invocadorComandosOrden,
            OrdenRepository ordenRepository,
            CalculadorPrecioOrden calculadorPrecioOrden) {
        this.invocadorComandosOrden = invocadorComandosOrden;
        this.ordenRepository = ordenRepository;
        this.calculadorPrecioOrden = calculadorPrecioOrden;
    }

    @Transactional
    public OrdenResponse colocarOrden(OrdenCreateRequest solicitud) {
        ComandoOrden comando = new ComandoColocarOrden(solicitud, ordenRepository, calculadorPrecioOrden);
        return invocadorComandosOrden.ejecutar(comando);
    }
}
