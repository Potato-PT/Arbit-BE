package com.arbit.app.recommendation.controller;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.common.response.ErrorResponse;
import com.arbit.app.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.LocalDate;
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
            description = "Returns the authenticated user's personalized recommendation list for the home screen, including match scores.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Recommendations retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RecommendationApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": [
                                                        {
                                                          "title": "Echoes of Silence",
                                                          "category": "Media Art",
                                                          "posterImageUrl": "https://cdn.arbit.app/events/light-museum/poster.jpg",
                                                          "url": "전시/공연 홈페이지 주소",
                                                          "venue": "Metropolitan Museum",
                                                          "startDate": "2026-05-01",
                                                          "endDate": "2026-06-30",
                                                          "free": false,
                                                          "status": "ONGOING",
                                                          "matchScore": 97.50,
                                                          "bookmarked": true
                                                        }
                                                      ],
                                                      "error": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Authentication is required. redirect to GET /api/home",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RecommendationErrorApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": false,
                                                      "data": null,
                                                      "error": {
                                                        "code": "UNAUTHORIZED",
                                                        "message": "Authentication is required. redirect to GET /api/home"
                                                      }
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public ApiResponse<List<com.arbit.app.recommendation.dto.RecommendedEventResponse>> getRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(recommendationService.getRecommendations(userDetails));
    }

    @Schema(description = "Wrapped recommendation response")
    private record RecommendationApiResponse(
            boolean success,
            List<RecommendationSwaggerItem> data,
            ErrorResponse error
    ) {
    }

    @Schema(description = "Wrapped recommendation error response")
    private record RecommendationErrorApiResponse(
            boolean success,
            Object data,
            ErrorResponse error
    ) {
    }

    @Schema(description = "Recommendation event item")
    private record RecommendationSwaggerItem(
            String title,
            String category,
            String posterImageUrl,
            String url,
            String venue,
            LocalDate startDate,
            LocalDate endDate,
            boolean free,
            String status,
            BigDecimal matchScore,
            boolean bookmarked
    ) {
    }
}
