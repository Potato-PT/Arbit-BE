package com.arbit.app.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Profile image update response")
public record UpdateProfileImageResponse(
        @Schema(description = "Updated profile image URL", example = "/uploads/users/8f2c/profile/2a6d-profile.jpg")
        String profileImageUrl
) {
}
