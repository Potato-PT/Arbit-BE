package com.arbit.app.event.dto;

import com.arbit.app.event.entity.EventStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Event detail response")
public record EventDetailResponse(
        @Schema(description = "Event title", example = "Echoes of Silence")
        String title,
        @Schema(description = "Top-level genre category", example = "Media Art")
        String category,
        @Schema(description = "Event poster image URL", example = "https://cdn.arbit.app/events/light-museum/poster.jpg")
        String posterImageUrl,
        @Schema(description = "External official page or ticketing URL", example = "https://example.com/events/echoes-of-silence")
        String url,
        @Schema(description = "Borough-level region", example = "Jongno-gu")
        String district,
        @Schema(description = "Venue name", example = "Metropolitan Museum")
        String venue,
        @Schema(description = "Event start date", example = "2026-05-01")
        LocalDate startDate,
        @Schema(description = "Event end date", example = "2026-06-30")
        LocalDate endDate,
        @Schema(description = "Admission fee description. null if not available.", example = "전석 20,000", nullable = true)
        String fee,
        @Schema(description = "Operating hours. null if not available.", example = "10:00 ~ 18:00 (입장마감 17:30)", nullable = true)
        String time,
        @Schema(description = "Whether the event is free of charge", example = "false")
        boolean free,
        @ArraySchema(schema = @Schema(description = "Subcategory or mood tag", example = "회화"))
        List<String> tag,
        @Schema(description = "Computed server-side from today's date", example = "ONGOING")
        EventStatus status,
        @Schema(description = "Average rating from user reviews. null if no reviews exist yet.", example = "4.7", nullable = true)
        BigDecimal rating,
        @Schema(description = "Whether the authenticated user bookmarked this event. Always false for unauthenticated requests.", example = "true")
        boolean bookmarked
) {
}
