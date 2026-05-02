package com.marketplace.order.command;

import com.marketplace.order.domain.BorradorOrden;
import com.marketplace.order.dto.LineaOrdenRequest;
import com.marketplace.order.dto.OrdenCreateRequest;
import com.marketplace.order.dto.DesglosePrecioOrden;
import com.marketplace.order.dto.OrdenResponse;
import com.marketplace.order.entity.Orden;
import com.marketplace.order.entity.OrdenLinea;
import com.marketplace.order.pricing.CalculadoraDesglosePrecioOrden;
import com.marketplace.order.repository.OrdenRepository;

import java.math.BigDecimal;

/**
 * Propósito: comando concreto que materializa una orden persistida a partir del DTO de entrada.
 * Patrón: Command concreto (conoce repositorio y política de precios ya decorada).
 * Responsabilidad: calcular total y desglose con {@link CalculadoraDesglosePrecioOrden}, mapear líneas y guardar el agregado.
 */
public class ComandoColocarOrden implements ComandoOrden {

    private final OrdenCreateRequest solicitud;
    private final OrdenRepository ordenRepository;
    private final CalculadoraDesglosePrecioOrden calculadoraDesglosePrecioOrden;

    public ComandoColocarOrden(
            OrdenCreateRequest solicitud,
            OrdenRepository ordenRepository,
            CalculadoraDesglosePrecioOrden calculadoraDesglosePrecioOrden) {
        this.solicitud = solicitud;
        this.ordenRepository = ordenRepository;
        this.calculadoraDesglosePrecioOrden = calculadoraDesglosePrecioOrden;
    }

    @Override
    public OrdenResponse ejecutar() {
        String pais = blancoANulo(solicitud.getPaisEnvio());
        String ciudad = blancoANulo(solicitud.getCiudadEnvio());
        String direccion = blancoANulo(solicitud.getDireccionEnvio());
        BorradorOrden borrador = new BorradorOrden(solicitud.getLineas(), pais, ciudad, direccion);
        DesglosePrecioOrden desglose = calculadoraDesglosePrecioOrden.desglose(borrador);
        BigDecimal total = desglose.total();

        Orden orden = new Orden();
        orden.setClienteId(solicitud.getClienteId().trim());
        orden.setPaisEnvio(pais);
        orden.setCiudadEnvio(ciudad);
        orden.setDireccionEnvio(direccion);
        orden.setTipoEntrega(normalizarTipoEntrega(solicitud.getTipoEntrega()));
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
                guardada.getPaisEnvio(),
                guardada.getCiudadEnvio(),
                guardada.getDireccionEnvio(),
                desglose.subtotalBase(),
                desglose.montoIva(),
                desglose.montoComision(),
                desglose.montoEnvio(),
                guardada.getTotal(),
                guardada.getTipoEntrega(),
                "CREADA",
                guardada.getCreadoEn());
    }

    private static String normalizarTipoEntrega(String raw) {
        if (raw == null || raw.isBlank()) {
            return "DOMICILIO";
        }
        String u = raw.trim().toUpperCase();
        if ("RECOGIDA".equals(u) || "DOMICILIO".equals(u)) {
            return u;
        }
        throw new IllegalArgumentException("tipoEntrega debe ser RECOGIDA o DOMICILIO.");
    }

    private static String blancoANulo(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
