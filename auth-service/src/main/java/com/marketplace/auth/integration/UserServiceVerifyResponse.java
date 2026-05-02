package com.marketplace.auth.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserServiceVerifyResponse(String nombreUsuario, List<String> roles) {}
