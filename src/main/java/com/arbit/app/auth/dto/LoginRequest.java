package com.arbit.app.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Username", example = "arbit_user_01")
        @NotBlank(message = "username must not be blank")
        String username,

        @Schema(description = "Password", example = "password1234")
        @NotBlank
        String password
) {
}
