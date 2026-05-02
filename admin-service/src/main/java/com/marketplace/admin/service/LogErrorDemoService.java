package com.marketplace.admin.service;

import com.marketplace.admin.dto.LogErrorItemResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class LogErrorDemoService {

    public List<LogErrorItemResponse> ultimos() {
        return List.of(
                new LogErrorItemResponse(
                        Instant.parse("2026-04-21T14:22:00Z"),
                        "WARN",
                        "Timeout hacia payment-service en entorno docker",
                        "solicitud-service"),
                new LogErrorItemResponse(
                        Instant.parse("2026-04-21T09:01:00Z"),
                        "ERROR",
                        "RestClientException al publicar SOLICITUD_MORA",
                        "solicitud-service"));
    }
}
