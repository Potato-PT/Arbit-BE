package com.arbit.app.review.service;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import com.arbit.app.event.entity.Event;
import com.arbit.app.event.repository.EventRepository;
import com.arbit.app.review.dto.CreateReviewRequest;
import com.arbit.app.review.dto.ReviewResponse;
import com.arbit.app.review.entity.Review;
import com.arbit.app.review.repository.ReviewRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final EventRepository eventRepository;

    public ReviewService(ReviewRepository reviewRepository, EventRepository eventRepository) {
        this.reviewRepository = reviewRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public ReviewResponse createReview(UUID eventId, CreateReviewRequest request, CustomUserDetails userDetails) {
        if (reviewRepository.existsByUserIdAndEventId(userDetails.id(), eventId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        Review review = Review.builder()
                .user(userDetails.user())
                .event(event)
                .rating(request.rating())
                .content(request.content())
                .verificationImageUrl(request.verificationImageUrl())
                .build();
        Review savedReview = reviewRepository.save(review);
        updateAverageRating(event);
        return toResponse(savedReview);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviews(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new BusinessException(ErrorCode.EVENT_NOT_FOUND);
        }
        return reviewRepository.findAllByEventIdOrderByCreatedAtDesc(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void updateAverageRating(Event event) {
        double averageRating = reviewRepository.averageRatingByEventId(event.getId());
        event.updateAverageRating(BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP));
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getRating(),
                review.getContent(),
                review.getVerificationImageUrl(),
                review.getCreatedAt()
        );
    }
}
