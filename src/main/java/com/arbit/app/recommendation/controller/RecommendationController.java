package com.arbit.app.recommendation.controller;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.recommendation.dto.RecommendationResponse;
import com.arbit.app.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "추천", description = "사용자 맞춤 이벤트 추천 결과를 조회합니다.")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    @Operation(
            summary = "사용자에게 맞는 추천 이벤트 목록을 조회합니다.",
            description = "사용자 취향과 추천 점수를 기준으로 개인화된 이벤트 목록을 조회합니다."
    )
    public ApiResponse<List<RecommendationResponse>> getRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(recommendationService.getRecommendations(userDetails));
    }
}
