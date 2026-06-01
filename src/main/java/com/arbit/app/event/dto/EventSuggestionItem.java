package com.arbit.app.event.dto;

import com.arbit.app.event.entity.Event;
import com.arbit.app.event.entity.EventStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Event search suggestion item")
public record EventSuggestionItem(
        @JsonProperty("event_id")
        @Schema(description = "Event UUID", example = "de4693dd-7196-4298-9bd9-c6c2a597b6b6")
        UUID eventId,
        @Schema(description = "Event title", example = "[서울남산국악당] 2026 남산 국악위크 [Vocal Space '조각눈']")
        String title,
        @Schema(description = "Category name", example = "국악")
        String category,
        @Schema(description = "Venue", example = "서울남산국악당 크라운해태홀")
        String venue,
        @Schema(description = "District", example = "중구")
        String district,
        @Schema(description = "Event start date", example = "2026-06-06")
        LocalDate startDate,
        @Schema(description = "Event end date", example = "2026-06-06")
        LocalDate endDate,
        @Schema(description = "Poster image URL from the event row", example = "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=3f3433c726c34bfbbca16ea200f7cb86&thumb=Y")
        String posterImageUrl,
        @Schema(description = "Admission price description", example = "전석 30,000원")
        String price,
        @Schema(description = "Whether the event is free", example = "false")
        boolean free,
        @Schema(description = "Computed event status", example = "UPCOMING")
        EventStatus status,
        @Schema(description = "Field that matched the keyword", example = "TITLE")
        EventSearchTarget matchedField,
        @Schema(description = "Text from the matched field", example = "[서울남산국악당] 2026 남산 국악위크 [Vocal Space '조각눈']")
        String highlightText
) {

    public static EventSuggestionItem from(Event event, EventSearchTarget matchedField, String highlightText) {
        return new EventSuggestionItem(
                event.getId(),
                event.getTitle(),
                event.getCategory().getName(),
                event.getVenue(),
                event.getDistrict(),
                event.getStartDate(),
                event.getEndDate(),
                event.getPosterImageUrl(),
                event.getPrice(),
                event.isFree(),
                EventStatus.from(event.getStartDate(), event.getEndDate(), EventStatus.today()),
                matchedField,
                highlightText
        );
    }
}
