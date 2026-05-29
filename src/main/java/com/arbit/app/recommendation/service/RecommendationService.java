package com.arbit.app.recommendation.service;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.bookmark.repository.BookmarkRepository;
import com.arbit.app.recommendation.dto.RecommendedEventResponse;
import com.arbit.app.recommendation.entity.Recommendation;
import com.arbit.app.recommendation.repository.RecommendationRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

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
        long startedAt = System.nanoTime();

        log.info("Loading home recommendations. userId={}", userId);

        List<UUID> bookmarkedEventIdList = bookmarkRepository.findEventIdsByUserId(userId);
        Set<UUID> bookmarkedEventIds = new HashSet<>(bookmarkedEventIdList);
        log.info("Loaded bookmarked event ids for home recommendations. userId={}, bookmarkCount={}",
                userId, bookmarkedEventIds.size());

        List<Recommendation> recommendations = recommendationRepository.findByUserIdOrderByMatchScoreDesc(userId);
        log.info("Loaded recommendation rows. userId={}, recommendationCount={}",
                userId, recommendations.size());

        recommendations.forEach(recommendation -> log.info(
                "Home recommendation row. userId={}, recommendationId={}, eventId={}, matchScore={}, bookmarked={}",
                userId,
                recommendation.getId(),
                recommendation.getEvent().getId(),
                recommendation.getMatchScore(),
                bookmarkedEventIds.contains(recommendation.getEvent().getId())
        ));

        List<RecommendedEventResponse> response = recommendations.stream()
                .map(recommendation -> RecommendedEventResponse.from(
                        recommendation.getEvent(),
                        recommendation.getMatchScore(),
                        bookmarkedEventIds.contains(recommendation.getEvent().getId())
                ))
                .toList();

        log.info("Mapped home recommendations. userId={}, responseCount={}, elapsedMs={}",
                userId, response.size(), elapsedMillis(startedAt));
        return response;
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
