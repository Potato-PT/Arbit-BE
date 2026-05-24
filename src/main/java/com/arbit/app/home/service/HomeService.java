package com.arbit.app.home.service;

import com.arbit.app.home.dto.HomeResponse;
import com.arbit.app.recommendation.dto.RecommendationResponse;
import com.arbit.app.recommendation.repository.RecommendationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HomeService {

    private final RecommendationRepository recommendationRepository;

    public HomeService(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @Transactional(readOnly = true)
    public HomeResponse getHome(UUID userId) {
        List<RecommendationResponse> recommendedEvents = recommendationRepository
                .findByUserIdOrderByMatchScoreDesc(userId).stream()
                .map(RecommendationResponse::from)
                .toList();

        return new HomeResponse(recommendedEvents);
    }
}
