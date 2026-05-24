package com.arbit.app.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "My profile response")
public record MyProfileResponse(
        @Schema(description = "Profile image URL", example = "https://cdn.arbit.app/users/8f2c/profile.jpg")
        String profileImageUrl,

        @Schema(description = "Nickname", example = "ArbitUser")
        String nickname,

        @Schema(description = "Subscription date", example = "2026-05-11T15:03:12")
        LocalDateTime subscribedAt,

        @Schema(description = "Taste keywords", example = "[\"Media Art\", \"Immersive\", \"Jazz\"]")
        List<String> tasteKeywords
) {
}
