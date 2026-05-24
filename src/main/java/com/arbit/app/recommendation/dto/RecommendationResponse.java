package com.arbit.app.recommendation.dto;

import com.arbit.app.event.dto.EventResponse;
import com.arbit.app.recommendation.entity.Recommendation;

public record RecommendationResponse(
        EventResponse event
) {

    public static RecommendationResponse from(Recommendation recommendation) {
        return new RecommendationResponse(
                EventResponse.from(recommendation.getEvent())
        );
    }
}
