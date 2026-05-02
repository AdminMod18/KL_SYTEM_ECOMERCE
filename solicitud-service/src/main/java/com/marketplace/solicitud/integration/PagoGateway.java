package com.marketplace.solicitud.integration;

import com.marketplace.solicitud.dto.ActivacionVendedorRequest;

/**
 * Cliente hacia {@code payment-service} ({@code POST /pagos}).
 */
public interface PagoGateway {

    /**
     * Ejecuta el cobro con el mismo contrato que expone payment-service.
     *
     * @param solicitudId usado para referencia por defecto y trazas
     */
    PagoRemotoResult procesarPago(ActivacionVendedorRequest request, long solicitudId);

    /**
     * Estados de éxito devueltos por las estrategias de pago (ONLINE / TARJETA / CONSIGNACION).
     */
    default boolean esPagoExitoso(String estadoRespuesta) {
        if (estadoRespuesta == null) {
            return false;
        }
        String e = estadoRespuesta.trim();
        return "AUTORIZADO".equals(e) || "CAPTURADO".equals(e) || "RECIBIDO".equals(e);
    }
}
