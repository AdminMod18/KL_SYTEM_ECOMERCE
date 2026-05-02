package com.marketplace.order.service;

import com.marketplace.order.command.ComandoColocarOrden;
import com.marketplace.order.command.ComandoOrden;
import com.marketplace.order.command.InvocadorComandosOrden;
import com.marketplace.order.dto.OrdenCreateRequest;
import com.marketplace.order.dto.OrdenListItemResponse;
import com.marketplace.order.dto.OrdenResponse;
import com.marketplace.order.pricing.CalculadoraDesglosePrecioOrden;
import com.marketplace.order.repository.OrdenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .map(o -> new OrdenListItemResponse(
                        o.getId(),
                        o.getClienteId(),
                        o.getTotal(),
                        tipoEntregaMostrado(o.getTipoEntrega()),
                        o.getCreadoEn(),
                        o.getLineas() == null ? 0 : o.getLineas().size()))
                .toList();
    }

    private static String tipoEntregaMostrado(String raw) {
        if (raw == null || raw.isBlank()) {
            return "DOMICILIO";
        }
        return raw;
    }
}
