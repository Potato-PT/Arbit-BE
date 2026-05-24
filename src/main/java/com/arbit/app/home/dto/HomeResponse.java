package com.arbit.app.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Home screen response")
public record HomeResponse(
        @Schema(description = "Latest registered events for the home screen")
        List<HomeEventResponse> events
) {
}
