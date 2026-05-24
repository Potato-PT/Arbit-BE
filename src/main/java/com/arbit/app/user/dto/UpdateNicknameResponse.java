package com.arbit.app.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Nickname update response")
public record UpdateNicknameResponse(
        @Schema(description = "Updated nickname", example = "CultureHunter")
        String nickname
) {
}
