package com.arbit.app.recommendation.service;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.recommendation.dto.RecommendationResponse;
import com.arbit.app.recommendation.repository.RecommendationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;

    public RecommendationService(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecommendations(CustomUserDetails userDetails) {
        return recommendationRepository.findByUserIdOrderByMatchScoreDesc(userDetails.id()).stream()
                .map(RecommendationResponse::from)
                .toList();
    }
}
