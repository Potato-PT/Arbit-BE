package com.arbit.app.preference.service;

import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import com.arbit.app.event.entity.Event;
import com.arbit.app.event.repository.EventRepository;
import com.arbit.app.recommendation.entity.Recommendation;
import com.arbit.app.recommendation.repository.RecommendationRepository;
import com.arbit.app.user.entity.User;
import com.arbit.app.user.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class PreferenceRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(PreferenceRecommendationService.class);
    private static final int RECOMMENDATION_LIMIT = 10;

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RecommendationRepository recommendationRepository;
    private final RestClient arbitAiRestClient;

    public PreferenceRecommendationService(UserRepository userRepository,
                                           EventRepository eventRepository,
                                           RecommendationRepository recommendationRepository,
                                           RestClient.Builder restClientBuilder,
                                           ArbitAiProperties arbitAiProperties) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.recommendationRepository = recommendationRepository;
        this.arbitAiRestClient = restClientBuilder
                .baseUrl(arbitAiProperties.baseUrl())
                .build();
    }

    @Transactional
    public void createRecommendations(UUID userId, List<UUID> eventIds) {
        log.info("Recommendation creation started. userId={}, inputEventCount={}, eventIds={}",
                userId, eventIds.size(), eventIds);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

            RecommendationResponse response = arbitAiRestClient.post()
                    .uri("/recommendations")
                    .body(new RecommendationRequest(eventIds, RECOMMENDATION_LIMIT))
                    .retrieve()
                    .body(RecommendationResponse.class);

            if (response == null || response.recommendations() == null) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to create recommendations.");
            }

            List<Recommendation> recommendations = toRecommendations(user, eventIds, response.recommendations());
            if (recommendations.isEmpty()) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "No AI recommendations matched local events.");
            }

            recommendationRepository.deleteAllByUserId(userId);
            recommendationRepository.flush();
            recommendationRepository.saveAll(recommendations);
            log.info("Recommendation creation completed. userId={}, savedCount={}", userId, recommendations.size());
        } catch (RestClientResponseException exception) {
            throw logAndWrapAiResponse(userId, eventIds, exception);
        } catch (RestClientException | IllegalArgumentException exception) {
            throw logAndWrap(userId, eventIds, exception);
        } catch (RuntimeException exception) {
            log.error("Recommendation creation failed. userId={}, eventIds={}", userId, eventIds, exception);
            throw exception;
        }
    }

    private BusinessException logAndWrapAiResponse(UUID userId, List<UUID> eventIds,
                                                   RestClientResponseException exception) {
        log.error(
                "AI recommendation request failed. userId={}, eventIds={}, statusCode={}, responseBody={}",
                userId,
                eventIds,
                exception.getStatusCode(),
                exception.getResponseBodyAsString(),
                exception
        );
        return new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to create recommendations.");
    }

    private BusinessException logAndWrap(UUID userId, List<UUID> eventIds, RuntimeException exception) {
        log.error("Recommendation creation failed. userId={}, eventIds={}", userId, eventIds, exception);
        return new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to create recommendations.");
    }

    private List<Recommendation> toRecommendations(User user, List<UUID> inputEventIds,
                                                   List<RecommendedEvent> recommendedEvents) {
        Map<UUID, Event> localEvents = findLocalEvents(recommendedEvents);

        return recommendedEvents.stream()
                .map(recommendedEvent -> toRecommendation(user, inputEventIds, recommendedEvent, localEvents))
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<UUID, Event> findLocalEvents(List<RecommendedEvent> recommendedEvents) {
        List<UUID> eventIds = recommendedEvents.stream()
                .map(RecommendedEvent::eventId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return eventRepository.findAllById(eventIds).stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        Function.identity(),
                        (first, ignored) -> first
                ));
    }

    private Recommendation toRecommendation(User user, List<UUID> inputEventIds, RecommendedEvent recommendedEvent,
                                            Map<UUID, Event> localEvents) {
        Event event = localEvents.get(recommendedEvent.eventId());
        if (event == null) {
            return null;
        }

        return Recommendation.builder()
                .user(user)
                .event(event)
                .matchScore(toMatchScore(recommendedEvent.matchPct()))
                .reason(toReason(recommendedEvent, inputEventIds))
                .build();
    }

    private String toReason(RecommendedEvent recommendedEvent, List<UUID> inputEventIds) {
        String genre = recommendedEvent.genre();
        String baseReason = genre == null || genre.isBlank()
                ? "Selected preference events matched this event."
                : "Selected preference events matched the " + genre + " genre.";
        return baseReason + " inputEventIds=" + inputEventIds;
    }

    private BigDecimal toMatchScore(Double value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value);
    }

    private record RecommendationRequest(
            @JsonProperty("event_ids") List<UUID> eventIds,
            int limit
    ) {
    }

    private record RecommendationResponse(
            @JsonProperty("candidates_count") int candidatesCount,
            List<RecommendedEvent> recommendations
    ) {
    }

    private record RecommendedEvent(
            @JsonProperty("event_id") UUID eventId,
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
