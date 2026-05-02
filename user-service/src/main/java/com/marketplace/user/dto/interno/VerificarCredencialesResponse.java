package com.marketplace.user.dto.interno;

import java.util.List;

public record VerificarCredencialesResponse(String nombreUsuario, List<String> roles) {}
