package com.arbit.app.review.controller;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.review.dto.CreateReviewRequest;
import com.arbit.app.review.dto.ReviewResponse;
import com.arbit.app.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events/{eventId}/reviews")
@Tag(name = "Review", description = "Review APIs for exhibitions and performances.")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    @Operation(
            summary = "Review list",
            description = "Returns exhibition or performance reviews for the specified event in latest-first order, including star ratings.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Review list retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ReviewListApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": [
                                                        {
                                                          "id": 18,
                                                          "rating": 5,
                                                          "content": "The stage direction and live arrangement were excellent.",
                                                          "verificationImageUrl": "https://cdn.arbit.app/reviews/18/ticket.jpg",
                                                          "createdAt": "2026-05-25T20:15:00"
                                                        },
                                                        {
                                                          "id": 11,
                                                          "rating": 4,
                                                          "content": "Strong exhibition flow and a well-designed final room.",
                                                          "verificationImageUrl": null,
                                                          "createdAt": "2026-05-24T09:10:00"
                                                        }
                                                      ],
                                                      "error": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Event not found")
            }
    )
    public ApiResponse<List<ReviewResponse>> getReviews(
            @Parameter(description = "Event UUID to retrieve reviews for", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID eventId) {
        return ApiResponse.success(List.of());
    }

    @PostMapping
    @Operation(
            summary = "Write a review",
            description = "Creates a review for the specified event with a 1 to 5 star rating and review text up to 200 characters.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateReviewRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "rating": 5,
                                              "content": "The immersive installation was worth the visit.",
                                              "verificationImageUrl": "https://cdn.arbit.app/reviews/uploaded-ticket.jpg"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Review created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ReviewCreateApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": {
                                                        "id": 19,
                                                        "rating": 5,
                                                        "content": "The immersive installation was worth the visit.",
                                                        "verificationImageUrl": "https://cdn.arbit.app/reviews/uploaded-ticket.jpg",
                                                        "createdAt": "2026-05-25T20:30:00"
                                                      },
                                                      "error": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Rating or content is invalid"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Event not found"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Review already exists for this event")
            }
    )
    public ApiResponse<ReviewResponse> createReview(
            @Parameter(description = "Event UUID to write a review for", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID eventId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateReviewRequest request) {
        return ApiResponse.success(reviewService.createReview(eventId, request, userDetails));
    }

    @Schema(description = "Wrapped review list response")
    private record ReviewListApiResponse(
            boolean success,
            @ArraySchema(schema = @Schema(implementation = ReviewResponse.class))
            List<ReviewResponse> data,
            Object error
    ) {
    }

    @Schema(description = "Wrapped review create response")
    private record ReviewCreateApiResponse(
            boolean success,
            @Schema(implementation = ReviewResponse.class)
            ReviewResponse data,
            Object error
    ) {
    }
}
