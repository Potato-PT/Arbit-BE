package com.arbit.app.recommendation.service;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import com.arbit.app.event.entity.EventStatus;
import com.arbit.app.preference.service.ArbitAiProperties;
import com.arbit.app.recommendation.dto.RecommendedEventResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class RecommendationService {

    private static final int RECOMMENDATION_LIMIT = 10;
    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final RestClient arbitAiRestClient;

    public RecommendationService(RestClient.Builder restClientBuilder,
                                 ArbitAiProperties arbitAiProperties) {
        this.arbitAiRestClient = restClientBuilder
                .baseUrl(arbitAiProperties.baseUrl())
                .build();
    }

    public List<RecommendedEventResponse> getRecommendations(CustomUserDetails userDetails, List<Integer> eventIds) {
        UUID userId = userDetails.id();
        long startedAt = System.nanoTime();

        validateEventIds(eventIds);

        log.info("Requesting home recommendations from AI server. userId={}, eventIds={}, limit={}",
                userId, eventIds, RECOMMENDATION_LIMIT);

        try {
            AiRecommendationResponse aiResponse = arbitAiRestClient.post()
                    .uri("/recommendations")
                    .body(new AiRecommendationRequest(eventIds, RECOMMENDATION_LIMIT))
                    .retrieve()
                    .body(AiRecommendationResponse.class);

            if (aiResponse == null || aiResponse.recommendations() == null) {
                log.error("AI recommendation response was empty. userId={}, eventIds={}, elapsedMs={}",
                        userId, eventIds, elapsedMillis(startedAt));
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to load recommendations from AI server.");
            }

            log.info("AI recommendation response received. userId={}, eventIds={}, candidatesCount={}, recommendationCount={}",
                    userId, eventIds, aiResponse.candidatesCount(), aiResponse.recommendations().size());

            aiResponse.recommendations().forEach(recommendation -> log.info(
                    "AI recommendation item. userId={}, aiEventId={}, title={}, genre={}, matchPct={}, status={}",
                    userId,
                    recommendation.eventId(),
                    recommendation.title(),
                    recommendation.genre(),
                    recommendation.matchPct(),
                    recommendation.status()
            ));

            List<RecommendedEventResponse> response = aiResponse.recommendations().stream()
                    .map(this::toRecommendedEventResponse)
                    .toList();

            log.info("Mapped AI recommendations for home response. userId={}, responseCount={}, elapsedMs={}",
                    userId, response.size(), elapsedMillis(startedAt));
            return response;
        } catch (RestClientException | IllegalArgumentException exception) {
            log.error("AI recommendation request failed. userId={}, eventIds={}, elapsedMs={}",
                    userId, eventIds, elapsedMillis(startedAt), exception);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to load recommendations from AI server.");
        }
    }

    private void validateEventIds(List<Integer> eventIds) {
        if (eventIds == null || eventIds.size() < 4 || eventIds.size() > 5) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "eventIds must contain 4 to 5 values.");
        }
    }

    private RecommendedEventResponse toRecommendedEventResponse(AiRecommendedEvent recommendation) {
        return new RecommendedEventResponse(
                recommendation.title(),
                recommendation.genre(),
                null,
                null,
                recommendation.district(),
                parseDate(recommendation.startDate()),
                parseDate(recommendation.endDate()),
                recommendation.free(),
                parseStatus(recommendation.status()),
                toMatchScore(recommendation.matchPct()),
                false
        );
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private EventStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return switch (value.toLowerCase()) {
            case "upcoming", "예정" -> EventStatus.UPCOMING;
            case "closed", "종료" -> EventStatus.CLOSED;
            case "ongoing", "진행중", "마감임박" -> EventStatus.ONGOING;
            default -> {
                log.warn("Unknown AI recommendation status received. status={}", value);
                yield EventStatus.ONGOING;
            }
        };
    }

    private BigDecimal toMatchScore(Double value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value);
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private record AiRecommendationRequest(
            @JsonProperty("event_ids") List<Integer> eventIds,
            int limit
    ) {
    }

    private record AiRecommendationResponse(
            @JsonProperty("candidates_count") int candidatesCount,
            List<AiRecommendedEvent> recommendations
    ) {
    }

    private record AiRecommendedEvent(
            @JsonProperty("event_id") Integer eventId,
            String title,
            String genre,
            String district,
            @JsonProperty("is_free") boolean free,
            @JsonProperty("start_date") String startDate,
            @JsonProperty("end_date") String endDate,
            String status,
            @JsonProperty("match_pct") Double matchPct
    ) {
    }
}
