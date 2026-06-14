package com.arbit.app.preference.service;

import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import com.arbit.app.event.entity.Event;
import com.arbit.app.event.repository.EventRepository;
import com.arbit.app.keyword.entity.Keyword;
import com.arbit.app.keyword.entity.KeywordType;
import com.arbit.app.keyword.entity.UserKeywordWeight;
import com.arbit.app.keyword.repository.KeywordRepository;
import com.arbit.app.keyword.repository.UserKeywordWeightRepository;
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
    private static final String AI_RECOMMENDATION_SOURCE = "ai_recommendation";

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RecommendationRepository recommendationRepository;
    private final KeywordRepository keywordRepository;
    private final UserKeywordWeightRepository userKeywordWeightRepository;
    private final RestClient arbitAiRestClient;

    public PreferenceRecommendationService(UserRepository userRepository,
                                           EventRepository eventRepository,
                                           RecommendationRepository recommendationRepository,
                                           KeywordRepository keywordRepository,
                                           UserKeywordWeightRepository userKeywordWeightRepository,
                                           RestClient.Builder restClientBuilder,
                                           ArbitAiProperties arbitAiProperties) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.recommendationRepository = recommendationRepository;
        this.keywordRepository = keywordRepository;
        this.userKeywordWeightRepository = userKeywordWeightRepository;
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

            if (response == null || response.preferenceProfile() == null || response.recommendations() == null) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to create recommendations.");
            }

            log.info("AI recommendation response received. userId={}, recommendationCount={}",
                    userId, response.recommendations().size());

            List<Recommendation> recommendations = toRecommendations(user, response.recommendations());
            if (recommendations.isEmpty()) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "No AI recommendations matched local events.");
            }

            List<UserKeywordWeight> keywordWeights = toKeywordWeights(user, response.preferenceProfile());

            userKeywordWeightRepository.deleteAllByUserId(userId);
            recommendationRepository.deleteAllByUserId(userId);
            userKeywordWeightRepository.flush();
            recommendationRepository.flush();
            userKeywordWeightRepository.saveAll(keywordWeights);
            recommendationRepository.saveAll(recommendations);
            log.info("Recommendation creation completed. userId={}, savedRecommendationCount={}, savedKeywordWeightCount={}",
                    userId, recommendations.size(), keywordWeights.size());
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
        if (exception.getStatusCode().is4xxClientError()) {
            return new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "Selected preference events are invalid or unavailable."
            );
        }
        return new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to create recommendations.");
    }

    private BusinessException logAndWrap(UUID userId, List<UUID> eventIds, RuntimeException exception) {
        log.error("Recommendation creation failed. userId={}, eventIds={}", userId, eventIds, exception);
        return new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to create recommendations.");
    }

    private List<Recommendation> toRecommendations(User user, List<RecommendedEvent> recommendedEvents) {
        Map<UUID, Event> localEvents = findLocalEvents(recommendedEvents);

        return recommendedEvents.stream()
                .map(recommendedEvent -> toRecommendation(user, recommendedEvent, localEvents))
                .filter(Objects::nonNull)
                .toList();
    }

    private List<UserKeywordWeight> toKeywordWeights(User user, PreferenceProfile profile) {
        return List.of(
                        new ProfileSection(KeywordType.CATEGORY, profile.genre()),
                        new ProfileSection(KeywordType.TOPIC, profile.subgenre()),
                        new ProfileSection(KeywordType.MOOD, profile.mood())
                ).stream()
                .flatMap(section -> section.weights().entrySet().stream()
                        .map(entry -> toKeywordWeight(user, section.type(), entry.getKey(), entry.getValue())))
                .toList();
    }

    private UserKeywordWeight toKeywordWeight(User user, KeywordType type, String value, BigDecimal weight) {
        Keyword keyword = keywordRepository.findByTypeAndValue(type, value)
                .orElseGet(() -> keywordRepository.save(Keyword.builder()
                        .type(type)
                        .value(value)
                        .build()));

        return UserKeywordWeight.builder()
                .user(user)
                .keyword(keyword)
                .weight(weight)
                .source(AI_RECOMMENDATION_SOURCE)
                .build();
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

    private Recommendation toRecommendation(User user, RecommendedEvent recommendedEvent,
                                            Map<UUID, Event> localEvents) {
        Event event = localEvents.get(recommendedEvent.eventId());
        if (event == null) {
            return null;
        }

        return Recommendation.builder()
                .user(user)
                .event(event)
                .matchScore(toMatchScore(recommendedEvent.preferenceMatch()))
                .reason(toReason(recommendedEvent))
                .build();
    }

    private String toReason(RecommendedEvent recommendedEvent) {
        return "AI preference profile matched this event.";
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
            @JsonProperty("preference_profile") PreferenceProfile preferenceProfile,
            List<RecommendedEvent> recommendations
    ) {
    }

    private record PreferenceProfile(
            Map<String, BigDecimal> genre,
            Map<String, BigDecimal> subgenre,
            Map<String, BigDecimal> mood
    ) {
        private PreferenceProfile {
            genre = genre == null ? Map.of() : genre;
            subgenre = subgenre == null ? Map.of() : subgenre;
            mood = mood == null ? Map.of() : mood;
        }
    }

    private record ProfileSection(KeywordType type, Map<String, BigDecimal> weights) {
    }

    private record RecommendedEvent(
            @JsonProperty("event_id") UUID eventId,
            @JsonProperty("total_score") Double totalScore,
            @JsonProperty("preference_match") Double preferenceMatch
    ) {
    }
}
