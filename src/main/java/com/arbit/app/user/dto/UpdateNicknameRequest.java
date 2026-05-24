package com.arbit.app.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Nickname update request")
public record UpdateNicknameRequest(
        @NotBlank
        @Size(max = 100)
        @Schema(description = "New nickname", example = "CultureHunter")
        String nickname
) {
}
