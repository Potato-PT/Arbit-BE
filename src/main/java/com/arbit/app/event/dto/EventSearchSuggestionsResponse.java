package com.arbit.app.event.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Event search suggestions response")
public record EventSearchSuggestionsResponse(
        @Schema(description = "Original search keyword", example = "국악위크")
        String keyword,
        @Schema(description = "Search target", example = "ALL")
        EventSearchTarget target,
        @ArraySchema(schema = @Schema(implementation = EventSuggestionItem.class))
        List<EventSuggestionItem> suggestions
) {
}
