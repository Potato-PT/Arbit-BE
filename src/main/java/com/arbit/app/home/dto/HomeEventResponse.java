package com.arbit.app.home.dto;

import com.arbit.app.event.entity.Event;
import com.arbit.app.event.entity.EventStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Home event item")
public record HomeEventResponse(
        @JsonProperty("event_id")
        @Schema(description = "Event UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID eventId,

        @Schema(description = "Event title", example = "Echoes of Silence")
        String title,

        @Schema(description = "Category name", example = "Media Art")
        String category,

        @Schema(description = "Poster image URL", example = "https://cdn.arbit.app/events/light-museum/poster.jpg")
        String posterImageUrl,

        @Schema(description = "External official page or ticketing URL", example = "https://example.com/events/echoes-of-silence")
        String url,

        @Schema(description = "Venue", example = "Metropolitan Museum")
        String venue,

        @Schema(description = "Event start date", example = "2026-05-01")
        LocalDate startDate,

        @Schema(description = "Event end date", example = "2026-06-30")
        LocalDate endDate,

        @Schema(description = "Whether the event is free", example = "false")
        boolean free,

        @Schema(description = "Event status", example = "ONGOING")
        EventStatus status
) {

    public static HomeEventResponse from(Event event) {
        return new HomeEventResponse(
                event.getId(),
                event.getTitle(),
                event.getCategory().getName(),
                event.getPosterImageUrl(),
                event.getBookingUrl(),
                event.getVenue(),
                event.getStartDate(),
                event.getEndDate(),
                event.isFree(),
                event.getStatus()
        );
    }
}
