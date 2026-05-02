package com.marketplace.admin.controller;

import com.marketplace.admin.dto.ParametroResponse;
import com.marketplace.admin.dto.ParametroUpdateRequest;
import com.marketplace.admin.service.ParametroSistemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin — parametros", description = "Parametrizacion persistida (H2 demo)")
@RestController
@RequestMapping("/admin/parametros")
public class AdminParametroController {

    private final ParametroSistemaService parametroSistemaService;

    public AdminParametroController(ParametroSistemaService parametroSistemaService) {
        this.parametroSistemaService = parametroSistemaService;
    }

    @Operation(summary = "Listar parametros")
    @GetMapping
    public List<ParametroResponse> listar() {
        return parametroSistemaService.listar();
    }

    @Operation(summary = "Actualizar parametro por clave")
    @PutMapping("/{clave}")
    public ParametroResponse actualizar(
            @PathVariable String clave, @Valid @RequestBody ParametroUpdateRequest request) {
        return parametroSistemaService.actualizar(clave, request);
    }
}
