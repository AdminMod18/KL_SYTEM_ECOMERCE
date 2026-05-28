package com.marketplace.order.service;

import com.marketplace.order.command.ComandoColocarOrden;
import com.marketplace.order.command.ComandoOrden;
import com.marketplace.order.command.InvocadorComandosOrden;
import com.marketplace.order.domain.BorradorOrden;
import com.marketplace.order.dto.DesglosePrecioOrden;
import com.marketplace.order.dto.LineaOrdenRequest;
import com.marketplace.order.dto.OrdenCreateRequest;
import com.marketplace.order.dto.OrdenLineaListItemResponse;
import com.marketplace.order.dto.OrdenListItemResponse;
import com.marketplace.order.dto.OrdenResponse;
import com.marketplace.order.entity.Orden;
import com.marketplace.order.entity.OrdenLinea;
import com.marketplace.order.pricing.CalculadoraDesglosePrecioOrden;
import com.marketplace.order.repository.OrdenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Propósito: capa de aplicación que construye comandos y los despacha al invocador.
 * Patrón: Application Service + Command client.
 * Responsabilidad: transaccionalidad y ensamblaje del {@link ComandoColocarOrden} con dependencias Spring.
 */
@Service
public class OrdenApplicationService {

    private static final Logger log = LoggerFactory.getLogger(OrdenApplicationService.class);

    private final InvocadorComandosOrden invocadorComandosOrden;
    private final OrdenRepository ordenRepository;
    private final CalculadoraDesglosePrecioOrden calculadoraDesglosePrecioOrden;

    public OrdenApplicationService(
            InvocadorComandosOrden invocadorComandosOrden,
            OrdenRepository ordenRepository,
            CalculadoraDesglosePrecioOrden calculadoraDesglosePrecioOrden) {
        this.invocadorComandosOrden = invocadorComandosOrden;
        this.ordenRepository = ordenRepository;
        this.calculadoraDesglosePrecioOrden = calculadoraDesglosePrecioOrden;
    }

    @Transactional
    public OrdenResponse colocarOrden(OrdenCreateRequest solicitud) {
        ComandoOrden comando = new ComandoColocarOrden(solicitud, ordenRepository, calculadoraDesglosePrecioOrden);
        OrdenResponse r = invocadorComandosOrden.ejecutar(comando);
        log.info(
                "Orden creada ordenId={} clienteId={} subtotalBase={} montoIva={} montoComision={} montoEnvio={} total={}",
                r.ordenId(),
                r.clienteId(),
                r.subtotalBase(),
                r.montoIva(),
                r.montoComision(),
                r.montoEnvio(),
                r.total());
        return r;
    }

    @Transactional(readOnly = true)
    public List<OrdenListItemResponse> listarPorCliente(String clienteId) {
        String cid = clienteId == null ? "" : clienteId.trim();
        return ordenRepository.findByClienteIdOrderByCreadoEnDesc(cid).stream()
                .map(this::aListItem)
                .toList();
    }

    private OrdenListItemResponse aListItem(Orden o) {
        List<OrdenLinea> lineasEntidad = o.getLineas() == null ? List.of() : o.getLineas();
        BorradorOrden borrador = borradorDesde(o, lineasEntidad);
        DesglosePrecioOrden desglose = calculadoraDesglosePrecioOrden.desglose(borrador);
        List<OrdenLineaListItemResponse> lineas = lineasEntidad.stream().map(this::aLineaListItem).toList();
        return new OrdenListItemResponse(
                o.getId(),
                o.getClienteId(),
                o.getTotal(),
                desglose.subtotalBase(),
                desglose.montoIva(),
                desglose.montoComision(),
                desglose.montoEnvio(),
                tipoEntregaMostrado(o.getTipoEntrega()),
                "CREADA",
                o.getCreadoEn(),
                lineas.size(),
                lineas);
    }

    private OrdenLineaListItemResponse aLineaListItem(OrdenLinea linea) {
        BigDecimal subtotal =
                linea.getPrecioUnitario().multiply(BigDecimal.valueOf(linea.getCantidad()));
        return new OrdenLineaListItemResponse(
                linea.getSku(), linea.getCantidad(), linea.getPrecioUnitario(), subtotal);
    }

    private static BorradorOrden borradorDesde(Orden o, List<OrdenLinea> lineasEntidad) {
        List<LineaOrdenRequest> lineas = lineasEntidad.stream()
                .map(l -> {
                    LineaOrdenRequest req = new LineaOrdenRequest();
                    req.setSku(l.getSku());
                    req.setCantidad(l.getCantidad());
                    req.setPrecioUnitario(l.getPrecioUnitario());
                    return req;
                })
                .toList();
        return new BorradorOrden(lineas, o.getPaisEnvio(), o.getCiudadEnvio(), o.getDireccionEnvio());
    }

    private static String tipoEntregaMostrado(String raw) {
        if (raw == null || raw.isBlank()) {
            return "DOMICILIO";
        }
        return raw;
    }
}
