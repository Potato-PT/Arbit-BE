package com.arbit.app.recommendation.dto;

import com.arbit.app.event.entity.Event;
import com.arbit.app.event.entity.EventStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Recommended event summary response")
public record RecommendedEventResponse(
        @JsonProperty("event_id")
        @Schema(description = "Event UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID eventId,

        @Schema(description = "Event title", example = "Echoes of Silence")
        String title,

        @Schema(description = "Category name", example = "Media Art")
        String category,

        @Schema(description = "Poster image URL", example = "https://cdn.arbit.app/events/light-museum/poster.jpg")
        String posterImageUrl,

        @Schema(description = "Event booking or detail URL", example = "https://tickets.example.com/events/550e8400", nullable = true)
        String url,

        @Schema(description = "Venue", example = "Metropolitan Museum")
        String venue,

        @Schema(description = "District", example = "Jongno-gu")
        String district,

        @Schema(description = "Event start date", example = "2026-05-01")
        LocalDate startDate,

        @Schema(description = "Event end date", example = "2026-06-30")
        LocalDate endDate,

        @Schema(description = "Whether the event is free", example = "false")
        boolean free,

        @Schema(description = "Admission price description", example = "전석 20,000", nullable = true)
        String price,

        @Schema(description = "Event status", example = "ONGOING")
        EventStatus status,

        @Schema(description = "Recommendation match score for this event", example = "97.50")
        BigDecimal matchScore,

        @Schema(description = "Whether the authenticated user bookmarked this event", example = "true")
        boolean bookmarked
) {

    public static RecommendedEventResponse from(Event event, BigDecimal matchScore, boolean bookmarked) {
        return new RecommendedEventResponse(
                event.getId(),
                event.getTitle(),
                event.getCategory().getName(),
                event.getPosterImageUrl(),
                event.getBookingUrl(),
                event.getVenue(),
                event.getDistrict(),
                event.getStartDate(),
                event.getEndDate(),
                event.isFree(),
                event.getPrice(),
                event.getStatus(),
                matchScore,
                bookmarked
        );
    }
}
