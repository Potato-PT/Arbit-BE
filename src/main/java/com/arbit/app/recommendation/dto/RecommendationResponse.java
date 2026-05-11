package com.arbit.app.recommendation.dto;

import com.arbit.app.event.dto.EventResponse;
import com.arbit.app.recommendation.entity.Recommendation;
import java.math.BigDecimal;

public record RecommendationResponse(
        EventResponse event,
        BigDecimal matchScore,
        String reason
) {

    public static RecommendationResponse from(Recommendation recommendation) {
        return new RecommendationResponse(
                EventResponse.from(recommendation.getEvent()),
                recommendation.getMatchScore(),
                recommendation.getReason()
        );
    }
}
