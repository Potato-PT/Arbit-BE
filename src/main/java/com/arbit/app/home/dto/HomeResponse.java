package com.arbit.app.home.dto;

import com.arbit.app.recommendation.dto.RecommendationResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Home screen response")
public record HomeResponse(
        @Schema(description = "Personalized recommended events")
        List<RecommendationResponse> recommendedEvents
) {
}
