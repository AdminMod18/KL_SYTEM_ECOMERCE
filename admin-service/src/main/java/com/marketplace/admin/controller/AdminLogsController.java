package com.marketplace.admin.controller;

import com.marketplace.admin.dto.LogErrorItemResponse;
import com.marketplace.admin.service.LogErrorDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin — logs", description = "Logs de error agregados (mock)")
@RestController
@RequestMapping("/admin/logs-error")
public class AdminLogsController {

    private final LogErrorDemoService logErrorDemoService;

    public AdminLogsController(LogErrorDemoService logErrorDemoService) {
        this.logErrorDemoService = logErrorDemoService;
    }

    @Operation(summary = "Ultimos errores demo")
    @GetMapping
    public List<LogErrorItemResponse> listar() {
        return logErrorDemoService.ultimos();
    }
}
