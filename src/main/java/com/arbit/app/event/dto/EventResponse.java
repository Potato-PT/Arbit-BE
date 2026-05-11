package com.arbit.app.event.dto;

import com.arbit.app.event.entity.Event;
import com.arbit.app.event.entity.EventStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String title,
        String category,
        String venue,
        String district,
        LocalDate startDate,
        LocalDate endDate,
        boolean free,
        EventStatus status,
        BigDecimal averageRating
) {

    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getCategory().getName(),
                event.getVenue(),
                event.getDistrict(),
                event.getStartDate(),
                event.getEndDate(),
                event.isFree(),
                event.getStatus(),
                event.getAverageRating()
        );
    }
}
