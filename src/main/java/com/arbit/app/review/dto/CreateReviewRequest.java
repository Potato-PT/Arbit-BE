package com.arbit.app.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @Min(1) @Max(5) int rating,
        @NotBlank @Size(max = 200) String content,
        @Size(max = 500) String verificationImageUrl
) {
}
