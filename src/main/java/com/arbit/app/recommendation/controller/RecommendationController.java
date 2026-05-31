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
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home/recommendations")
@Tag(name = "Home", description = "Home screen APIs for the authenticated user.")
@SecurityRequirement(name = "bearerAuth")
public class RecommendationController {

    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    @Operation(
            summary = "Get home recommendations",
            description = "Returns the authenticated user's stored personalized recommendations for the home screen.",
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
                                                          "category": "exhibition",
                                                          "posterImageUrl": null,
                                                          "venue": null,
                                                          "district": "Jongno-gu",
                                                          "startDate": "2026-05-01",
                                                          "endDate": "2026-06-30",
                                                          "free": false,
                                                          "status": "ONGOING",
                                                          "matchScore": 97.50,
                                                          "bookmarked": false
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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {
        long startedAt = System.nanoTime();
        String method = request.getMethod();
        String requestUri = request.getRequestURI();

        log.info("Home recommendation request received. method={}, uri={}, userId={}",
                method, requestUri, userDetails.id());

        try {
            List<com.arbit.app.recommendation.dto.RecommendedEventResponse> recommendations =
                    recommendationService.getRecommendations(userDetails);
            ApiResponse<List<com.arbit.app.recommendation.dto.RecommendedEventResponse>> response =
                    ApiResponse.success(recommendations);

            log.info("Home recommendation response ready. method={}, uri={}, userId={}, success={}, itemCount={}, elapsedMs={}",
                    method,
                    requestUri,
                    userDetails.id(),
                    response.success(),
                    recommendations.size(),
                    elapsedMillis(startedAt));
            return response;
        } catch (RuntimeException exception) {
            log.error("Home recommendation request failed. method={}, uri={}, userId={}, elapsedMs={}",
                    method, requestUri, userDetails.id(), elapsedMillis(startedAt), exception);
            throw exception;
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
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
            String venue,
            String district,
            LocalDate startDate,
            LocalDate endDate,
            boolean free,
            String status,
            BigDecimal matchScore,
            boolean bookmarked
    ) {
    }
}
