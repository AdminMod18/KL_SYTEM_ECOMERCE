package com.marketplace.validation.port;

import com.marketplace.validation.domain.ResultadoCifin;

/**
 * Propósito: contrato para interpretar reportes CIFIN suministrados como texto plano (archivo batch).
 * Patrón: Puerto (Hexagonal); lo implementa el adaptador de archivo plano.
 * Responsabilidad: aislar reglas de parseo y columnas del resto del sistema.
 */
public interface CifinArchivoPort {

    ResultadoCifin interpretarParaDocumento(String contenidoArchivoPlano, String documentoIdentidad);
}
