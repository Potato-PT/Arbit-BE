package com.arbit.app.preference.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Filtered event returned for choosing preferences")
public record PreferenceCategoriesResponse(
        @JsonProperty("event_id")
        @Schema(description = "Event identifier stored as BINARY(16)", format = "uuid", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID eventId,

        @Schema(description = "Event title", example = "Seoul Media Art Exhibition")
        String title,

        @Schema(description = "Event genre", example = "exhibition")
        String genre,

        @Schema(description = "Event poster image URL", example = "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png")
        String posterImage
) {
}
