package com.arbit.app.review.controller;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.review.dto.CreateReviewRequest;
import com.arbit.app.review.dto.ReviewResponse;
import com.arbit.app.review.service.ReviewService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events/{eventId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ApiResponse<ReviewResponse> createReview(@PathVariable UUID eventId,
                                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                                    @Valid @RequestBody CreateReviewRequest request) {
        return ApiResponse.success(reviewService.createReview(eventId, request, userDetails));
    }
}
