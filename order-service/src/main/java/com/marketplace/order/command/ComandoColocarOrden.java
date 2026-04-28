package com.marketplace.order.command;

import com.marketplace.order.domain.BorradorOrden;
import com.marketplace.order.dto.LineaOrdenRequest;
import com.marketplace.order.dto.OrdenCreateRequest;
import com.marketplace.order.dto.OrdenResponse;
import com.marketplace.order.entity.Orden;
import com.marketplace.order.entity.OrdenLinea;
import com.marketplace.order.pricing.CalculadorPrecioOrden;
import com.marketplace.order.repository.OrdenRepository;

import java.math.BigDecimal;

/**
 * Propósito: comando concreto que materializa una orden persistida a partir del DTO de entrada.
 * Patrón: Command concreto (conoce repositorio y política de precios ya decorada).
 * Responsabilidad: calcular total con {@link CalculadorPrecioOrden}, mapear líneas y guardar el agregado.
 */
public class ComandoColocarOrden implements ComandoOrden {

    private final OrdenCreateRequest solicitud;
    private final OrdenRepository ordenRepository;
    private final CalculadorPrecioOrden calculadorPrecioOrden;

    public ComandoColocarOrden(
            OrdenCreateRequest solicitud,
            OrdenRepository ordenRepository,
            CalculadorPrecioOrden calculadorPrecioOrden) {
        this.solicitud = solicitud;
        this.ordenRepository = ordenRepository;
        this.calculadorPrecioOrden = calculadorPrecioOrden;
    }

    @Override
    public OrdenResponse ejecutar() {
        BorradorOrden borrador = new BorradorOrden(solicitud.getLineas());
        BigDecimal total = calculadorPrecioOrden.calcular(borrador);

        Orden orden = new Orden();
        orden.setClienteId(solicitud.getClienteId().trim());
        orden.setTotal(total);

        for (LineaOrdenRequest lineaDto : solicitud.getLineas()) {
            OrdenLinea linea = new OrdenLinea();
            linea.setSku(lineaDto.getSku().trim());
            linea.setCantidad(lineaDto.getCantidad());
            linea.setPrecioUnitario(lineaDto.getPrecioUnitario());
            orden.agregarLinea(linea);
        }

        Orden guardada = ordenRepository.save(orden);
        return new OrdenResponse(
                guardada.getId(),
                guardada.getClienteId(),
                guardada.getTotal(),
                "CREADA",
                guardada.getCreadoEn());
    }
}
