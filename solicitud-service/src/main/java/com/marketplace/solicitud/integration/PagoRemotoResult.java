package com.marketplace.solicitud.integration;

import java.util.UUID;

/**
 * Resultado deserializado de {@code PagoResponse} del payment-service.
 */
public record PagoRemotoResult(UUID idTransaccion, String estado, String mensaje, String tipo) {
}
