package com.arbit.app.recommendation.service;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.bookmark.repository.BookmarkRepository;
import com.arbit.app.recommendation.dto.RecommendedEventResponse;
import com.arbit.app.recommendation.entity.Recommendation;
import com.arbit.app.recommendation.repository.RecommendationRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final BookmarkRepository bookmarkRepository;

    public RecommendationService(RecommendationRepository recommendationRepository,
                                 BookmarkRepository bookmarkRepository) {
        this.recommendationRepository = recommendationRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    @Transactional(readOnly = true)
    public List<RecommendedEventResponse> getRecommendations(CustomUserDetails userDetails) {
        UUID userId = userDetails.id();
        Set<UUID> bookmarkedEventIds = bookmarkRepository.findEventIdsByUserId(userId).stream()
                .collect(Collectors.toSet());

        return recommendationRepository.findByUserIdOrderByMatchScoreDesc(userId).stream()
                .map(recommendation -> toResponse(recommendation, bookmarkedEventIds))
                .toList();
    }

    private RecommendedEventResponse toResponse(Recommendation recommendation, Set<UUID> bookmarkedEventIds) {
        return RecommendedEventResponse.from(
                recommendation.getEvent(),
                recommendation.getMatchScore(),
                bookmarkedEventIds.contains(recommendation.getEvent().getId())
        );
    }
}
