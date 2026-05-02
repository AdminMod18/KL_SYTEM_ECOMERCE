package com.marketplace.admin.controller;

import com.marketplace.admin.dto.AuditoriaItemResponse;
import com.marketplace.admin.service.AuditoriaDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin — auditoria", description = "Eventos de auditoria (mock)")
@RestController
@RequestMapping("/admin/auditoria")
public class AdminAuditoriaController {

    private final AuditoriaDemoService auditoriaDemoService;

    public AdminAuditoriaController(AuditoriaDemoService auditoriaDemoService) {
        this.auditoriaDemoService = auditoriaDemoService;
    }

    @Operation(summary = "Listado demo de auditoria")
    @GetMapping
    public List<AuditoriaItemResponse> listar() {
        return auditoriaDemoService.listar();
    }
}
