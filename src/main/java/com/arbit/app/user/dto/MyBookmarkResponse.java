package com.arbit.app.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "My bookmark item")
public record MyBookmarkResponse(
        @JsonProperty("event_id")
        @Schema(description = "Event UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID eventId,

        @Schema(description = "Event title", example = "Light Museum Seoul")
        String title,

        @Schema(description = "Poster image URL", example = "https://cdn.arbit.app/events/light-museum/poster.jpg")
        String posterImageUrl,

        @Schema(description = "Category name", example = "Exhibition")
        String category,

        @Schema(description = "Venue", example = "DDP")
        String venue,

        @Schema(description = "Admission price description", example = "전석 20,000", nullable = true)
        String price,

        @Schema(description = "Event start date", example = "2026-05-01")
        LocalDate startDate,

        @Schema(description = "Event end date", example = "2026-06-30")
        LocalDate endDate,

        @Schema(description = "Bookmark created date", example = "2026-05-20T18:30:00")
        LocalDateTime bookmarkedAt
) {
}
