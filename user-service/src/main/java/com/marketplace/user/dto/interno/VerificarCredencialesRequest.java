package com.marketplace.user.dto.interno;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificarCredencialesRequest {

    @NotBlank
    @Size(max = 200)
    private String usernameOrEmail;

    @NotBlank
    @Size(max = 128)
    private String password;
}
