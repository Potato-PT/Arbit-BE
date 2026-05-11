package com.arbit.app.recommendation.controller;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.recommendation.dto.RecommendationResponse;
import com.arbit.app.recommendation.service.RecommendationService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ApiResponse<List<RecommendationResponse>> getRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(recommendationService.getRecommendations(userDetails));
    }
}
