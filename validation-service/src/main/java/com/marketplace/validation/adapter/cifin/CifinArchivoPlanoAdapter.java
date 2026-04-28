package com.marketplace.validation.adapter.cifin;

import com.marketplace.validation.domain.ResultadoCifin;
import com.marketplace.validation.port.CifinArchivoPort;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Propósito: interpretar archivos planos estilo CIFIN entregados como cadena multilínea.
 * Patrón: Adapter (traduce formato legacy batch {@code DOCUMENTO|RIESGO|ESTADO} al dominio).
 * Responsabilidad: localizar la fila del documento y tolerar líneas vacías o mal formadas con mensajes controlados.
 */
@Component
public class CifinArchivoPlanoAdapter implements CifinArchivoPort {

    private static final String SEPARADOR = "\\|";

    @Override
    public ResultadoCifin interpretarParaDocumento(String contenidoArchivoPlano, String documentoIdentidad) {
        if (contenidoArchivoPlano == null || contenidoArchivoPlano.isBlank()) {
            return new ResultadoCifin(0, "SIN_DATOS", false);
        }

        return Arrays.stream(contenidoArchivoPlano.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(line -> line.split(SEPARADOR))
                .filter(partes -> partes.length >= 3)
                .filter(partes -> documentoIdentidad.equalsIgnoreCase(partes[0].trim()))
                .findFirst()
                .map(partes -> {
                    try {
                        int riesgo = Integer.parseInt(partes[1].trim());
                        return new ResultadoCifin(riesgo, partes[2].trim(), true);
                    } catch (NumberFormatException ex) {
                        return new ResultadoCifin(0, "FORMATO_INVALIDO", false);
                    }
                })
                .orElseGet(() -> new ResultadoCifin(0, "NO_ENCONTRADO", false));
    }
}
