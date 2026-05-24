package com.arbit.app.recommendation.dto;

import com.arbit.app.recommendation.entity.Recommendation;

public record RecommendationResponse(
        RecommendedEventResponse event
) {

    public static RecommendationResponse from(Recommendation recommendation) {
        return new RecommendationResponse(
                RecommendedEventResponse.from(recommendation.getEvent())
        );
    }
}
