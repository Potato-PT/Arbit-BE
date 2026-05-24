package com.arbit.app.recommendation.controller;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.recommendation.dto.RecommendationResponse;
import com.arbit.app.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home/recommendations")
@Tag(name = "Home", description = "Home screen APIs for the authenticated user.")
@SecurityRequirement(name = "bearerAuth")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    @Operation(
            summary = "Get home recommendations",
            description = "Returns the authenticated user's personalized recommendation list for the home screen."
    )
    public ApiResponse<List<RecommendationResponse>> getRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(recommendationService.getRecommendations(userDetails));
    }
}
