package com.arbit.app.event.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Paged event search result response")
public record EventSearchResultsResponse(
        @Schema(description = "Original search keyword", example = "악뮤")
        String keyword,
        @Schema(description = "Search target", example = "ALL")
        EventSearchTarget target,
        @Schema(description = "Page number", example = "0")
        int page,
        @Schema(description = "Page size", example = "20")
        int size,
        @Schema(description = "Total matching elements", example = "1")
        long totalElements,
        @Schema(description = "Total pages", example = "1")
        int totalPages,
        @ArraySchema(schema = @Schema(implementation = EventSearchItem.class))
        List<EventSearchItem> items
) {
}
