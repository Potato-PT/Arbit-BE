package com.arbit.app.review.controller;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.review.dto.CreateReviewRequest;
import com.arbit.app.review.dto.ReviewResponse;
import com.arbit.app.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "리뷰", description = "이벤트에 대한 사용자 리뷰를 관리합니다.")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @Operation(
            summary = "이벤트에 대한 리뷰를 작성합니다.",
            description = "사용자가 특정 이벤트에 평점과 리뷰 내용을 등록합니다."
    )
    public ApiResponse<ReviewResponse> createReview(
                                                    @Parameter(description = "리뷰를 작성할 이벤트 ID")
                                                    @PathVariable UUID eventId,
                                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                                    @Valid @RequestBody CreateReviewRequest request) {
        return ApiResponse.success(reviewService.createReview(eventId, request, userDetails));
    }
}
