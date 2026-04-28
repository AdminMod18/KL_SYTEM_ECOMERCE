package com.marketplace.validation.port;

import com.marketplace.validation.domain.ResultadoDatacredito;

/**
 * Propósito: contrato de aplicación para obtener resultado crediticio sin acoplarse a HTTP ni proveedores.
 * Patrón: Puerto (Hexagonal / Ports & Adapters); lo implementa el adaptador REST de Datacrédito.
 * Responsabilidad: exponer una única operación estable para quien orquesta validaciones (Facade).
 */
public interface DatacreditoConsultaPort {

    ResultadoDatacredito consultarPorDocumento(String documentoIdentidad);
}
