package com.arbit.app.preference.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Filtered event returned for choosing preferences")
public record PreferenceCategoriesResponse(
        @JsonProperty("event_id")
        @Schema(description = "Event UUID from the shared events table", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID eventId,

        @Schema(description = "Event title", example = "Seoul Media Art Exhibition")
        String title,

        @Schema(description = "Event genre", example = "exhibition")
        String genre,

        @Schema(description = "Event poster image URL from the events table", example = "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=3f3433c726c34bfbbca16ea200f7cb86&thumb=Y")
        String posterImage
) {
}
