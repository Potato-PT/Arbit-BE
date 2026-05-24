package com.arbit.app.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "My review item")
public record MyReviewResponse(
        @Schema(description = "Review ID", example = "12")
        Long reviewId,

        @Schema(description = "Event title", example = "Light Museum Seoul")
        String title,

        @Schema(description = "Poster image URL", example = "https://cdn.arbit.app/events/light-museum/poster.jpg")
        String posterImageUrl,

        @Schema(description = "Star score", example = "5")
        int starScore,

        @Schema(description = "Review content", example = "The immersive media wall was the highlight.")
        String content,

        @Schema(description = "Number of likes", example = "0")
        long likes,

        @Schema(description = "Creation date", example = "2026-05-23T09:40:00")
        LocalDateTime createdAt
) {
}
