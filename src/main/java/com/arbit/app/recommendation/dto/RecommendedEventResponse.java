package com.arbit.app.recommendation.dto;

import com.arbit.app.event.entity.Event;
import com.arbit.app.event.entity.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Recommended event summary response")
public record RecommendedEventResponse(
        @Schema(description = "Event ID", example = "11111111-1111-1111-1111-111111111111")
        UUID id,

        @Schema(description = "Event title", example = "Echoes of Silence")
        String title,

        @Schema(description = "Category name", example = "Media Art")
        String category,

        @Schema(description = "Poster image URL", example = "https://cdn.arbit.app/events/light-museum/poster.jpg")
        String posterImageUrl,

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

        @Schema(description = "Event status", example = "ONGOING")
        EventStatus status
) {

    public static RecommendedEventResponse from(Event event) {
        return new RecommendedEventResponse(
                event.getId(),
                event.getTitle(),
                event.getCategory().getName(),
                event.getPosterImageUrl(),
                event.getVenue(),
                event.getDistrict(),
                event.getStartDate(),
                event.getEndDate(),
                event.isFree(),
                event.getStatus()
        );
    }
}
