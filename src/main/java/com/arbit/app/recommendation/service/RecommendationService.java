package com.arbit.app.recommendation.service;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.bookmark.repository.BookmarkRepository;
import com.arbit.app.recommendation.dto.RecommendedEventResponse;
import com.arbit.app.recommendation.repository.RecommendationRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        Set<java.util.UUID> bookmarkedEventIds = new HashSet<>(bookmarkRepository.findEventIdsByUserId(userDetails.id()));
        return recommendationRepository.findByUserIdOrderByMatchScoreDesc(userDetails.id()).stream()
                .map(recommendation -> RecommendedEventResponse.from(
                        recommendation.getEvent(),
                        recommendation.getMatchScore(),
                        bookmarkedEventIds.contains(recommendation.getEvent().getId())
                ))
                .toList();
    }
}
