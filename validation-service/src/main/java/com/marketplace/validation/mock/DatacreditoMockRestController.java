package com.marketplace.validation.mock;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Propósito: simular el endpoint REST de Datacrédito para desarrollo y pruebas sin credenciales reales.
 * Patrón: ninguno GoF (stub HTTP); consumido por {@link com.marketplace.validation.adapter.datacredito.DatacreditoRestAdapter}.
 * Responsabilidad: devolver JSON con score y lista de control según reglas simples sobre el documento.
 */
@Hidden
@RestController
@RequestMapping(path = "/internal/mock/datacredito", produces = MediaType.APPLICATION_JSON_VALUE)
public class DatacreditoMockRestController {

    @GetMapping("/{documento}")
    public DatacreditoMockPayload consultar(@PathVariable String documento) {
        boolean listaControl = documento.endsWith("999");
        int score;
        if (listaControl) {
            score = 300;
        } else if (documento.endsWith("555")) {
            score = 620;
        } else {
            score = 720;
        }
        return new DatacreditoMockPayload(
                documento,
                score,
                listaControl,
                "MOCK-DC-" + UUID.randomUUID());
    }

    public record DatacreditoMockPayload(
            String documento,
            int score,
            boolean listaControl,
            String referenciaConsulta
    ) {
    }
}
