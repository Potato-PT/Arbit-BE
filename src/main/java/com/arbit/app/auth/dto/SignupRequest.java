package com.arbit.app.auth.dto;

import com.arbit.app.user.entity.UserGender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @Schema(description = "Username", example = "arbit_user_01")
        @NotBlank(message = "username must not be blank")
        @Size(max = 50, message = "username must be 50 characters or fewer")
        String username,

        @Schema(description = "Password", example = "password1234")
        @NotBlank(message = "password must not be blank")
        @Size(min = 8, max = 100, message = "password must be between 8 and 100 characters")
        String password,

        @Schema(description = "Nickname", example = "ArbitUser")
        @NotBlank(message = "nickname must not be blank")
        @Size(max = 100, message = "nickname must be 100 characters or fewer")
        String nickname,

        @Schema(description = "Birth year", example = "1998")
        @Min(value = 1900, message = "birthYear must be at least 1900")
        @Max(value = 2100, message = "birthYear must be 2100 or less")
        Integer birthYear,

        @Schema(description = "Gender", example = "MALE", allowableValues = {"MALE", "FEMALE", "NONSELECT"})
        UserGender gender,

        @Schema(description = "Residential area", example = "Seoul Mapo-gu")
        @Size(max = 255, message = "residentialArea must be 255 characters or fewer")
        String residentialArea
) {
}
