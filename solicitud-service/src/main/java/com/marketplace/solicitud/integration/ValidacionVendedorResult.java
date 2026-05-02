package com.marketplace.solicitud.integration;

import com.marketplace.solicitud.model.SolicitudEstado;

/**
 * Resultado de validation-service: estado destino y texto para correo certificado (HU-09).
 */
public record ValidacionVendedorResult(SolicitudEstado estado, String detalleNotificacion) {}
