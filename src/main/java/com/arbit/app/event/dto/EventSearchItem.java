package com.arbit.app.event.dto;

import com.arbit.app.event.entity.Event;
import com.arbit.app.event.entity.EventStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Event search result item")
public record EventSearchItem(
        @JsonProperty("event_id")
        @Schema(description = "Event UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID eventId,
        @Schema(description = "Event title", example = "악뮤 콘서트 LOVE EPISODE")
        String title,
        @Schema(description = "Category name", example = "콘서트")
        String category,
        @Schema(description = "Poster image URL", example = "https://cdn.arbit.app/events/akmu/poster.jpg")
        String posterImageUrl,
        @Schema(description = "Venue", example = "올림픽공원")
        String venue,
        @Schema(description = "District", example = "송파구")
        String district,
        @Schema(description = "Event start date", example = "2026-06-03")
        LocalDate startDate,
        @Schema(description = "Event end date", example = "2026-06-30")
        LocalDate endDate,
        @Schema(description = "Whether the event is free", example = "false")
        boolean free,
        @Schema(description = "Admission price description", example = "18,600원")
        String price,
        @Schema(description = "Computed event status", example = "ONGOING")
        EventStatus status,
        @Schema(description = "Average rating", example = "4.7")
        BigDecimal averageRating
) {

    public static EventSearchItem from(Event event) {
        return new EventSearchItem(
                event.getId(),
                event.getTitle(),
                event.getCategory().getName(),
                event.getPosterImageUrl(),
                event.getVenue(),
                event.getDistrict(),
                event.getStartDate(),
                event.getEndDate(),
                event.isFree(),
                event.getPrice(),
                EventStatus.from(event.getStartDate(), event.getEndDate(), EventStatus.today()),
                event.getAverageRating()
        );
    }
}
