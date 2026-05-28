package com.arbit.app.preference.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Schema(description = "Choose-preferences request")
public record CreatePreferenceRequest(
        @NotNull
        @NotEmpty
        @JsonProperty("event_id")
        @Schema(description = "Selected event identifiers", example = "[1, 3, 7]")
        List<@NotNull @Positive Long> eventIds
) {
}
