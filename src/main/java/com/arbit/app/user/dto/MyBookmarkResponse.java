package com.arbit.app.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "My bookmark item")
public record MyBookmarkResponse(
        @Schema(description = "Event ID", example = "1")
        UUID eventId,

        @Schema(description = "Event title", example = "Light Museum Seoul")
        String title,

        @Schema(description = "Poster image URL", example = "https://cdn.arbit.app/events/light-museum/poster.jpg")
        String posterImageUrl,

        @Schema(description = "Category name", example = "Exhibition")
        String category,

        @Schema(description = "Venue", example = "DDP")
        String venue,

        @Schema(description = "Event start date", example = "2026-05-01")
        LocalDate startDate,

        @Schema(description = "Event end date", example = "2026-06-30")
        LocalDate endDate,

        @Schema(description = "Bookmark created date", example = "2026-05-20T18:30:00")
        LocalDateTime bookmarkedAt
) {
}
