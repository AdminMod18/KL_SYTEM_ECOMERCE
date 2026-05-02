package com.marketplace.product.integration;

/**
 * Verifica que la solicitud de vendedor (identificador de negocio) esté en estado operativo para publicar.
 */
public interface VendedorActivoPort {

    /**
     * @param solicitudId identificador de la solicitud en solicitud-service (vendedor)
     * @throws com.marketplace.product.exception.ProductoBusinessException si no está en ACTIVA
     */
    void assertVendedorEnEstadoActiva(long solicitudId);
}
