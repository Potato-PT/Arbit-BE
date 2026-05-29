package com.arbit.app.preference.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Filtered event returned for choosing preferences")
public record PreferenceCategoriesResponse(
        @JsonProperty("event_id")
        @Schema(description = "Event identifier from Arbit-AI", example = "12")
        Integer eventId,

        @Schema(description = "Event title", example = "Seoul Media Art Exhibition")
        String title,

        @Schema(description = "Event genre", example = "exhibition")
        String genre,

        @Schema(description = "Event poster image URL", example = "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png")
        String posterImage
) {
}
