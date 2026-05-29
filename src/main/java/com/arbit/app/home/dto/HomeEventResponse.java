package com.arbit.app.home.dto;

import com.arbit.app.event.entity.Event;
import com.arbit.app.event.entity.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Home event item")
public record HomeEventResponse(
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

        @Schema(description = "Admission price description", example = "전석 20,000", nullable = true)
        String price,

        @Schema(description = "Event status", example = "ONGOING")
        EventStatus status
) {

    public static HomeEventResponse from(Event event) {
        return new HomeEventResponse(
                event.getTitle(),
                event.getCategory().getName(),
                event.getPosterImageUrl(),
                event.getVenue(),
                event.getDistrict(),
                event.getStartDate(),
                event.getEndDate(),
                event.isFree(),
                event.getPrice(),
                event.getStatus()
        );
    }
}
