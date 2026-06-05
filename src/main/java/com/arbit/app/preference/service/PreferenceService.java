package com.arbit.app.preference.service;

import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import com.arbit.app.event.entity.Event;
import com.arbit.app.event.repository.EventRepository;
import com.arbit.app.preference.dto.PreferenceCategoriesResponse;
import com.arbit.app.preference.entity.UserPreferenceEvent;
import com.arbit.app.preference.repository.UserPreferenceEventRepository;
import com.arbit.app.user.entity.User;
import com.arbit.app.user.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class PreferenceService {

    private static final Logger log = LoggerFactory.getLogger(PreferenceService.class);
    private static final int SEED_EVENT_SAMPLE_SIZE = 10;
    private static final int RANDOM_STATE_BOUND = 1_000_000;

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final UserPreferenceEventRepository userPreferenceEventRepository;
    private final PreferenceRecommendationService recommendationService;
    private final Executor recommendationTaskExecutor;
    private final RestClient arbitAiRestClient;

    public PreferenceService(UserRepository userRepository,
                             EventRepository eventRepository,
                             UserPreferenceEventRepository userPreferenceEventRepository,
                             PreferenceRecommendationService recommendationService,
                             @Qualifier("recommendationTaskExecutor") Executor recommendationTaskExecutor,
                             RestClient.Builder restClientBuilder,
                             ArbitAiProperties arbitAiProperties) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.userPreferenceEventRepository = userPreferenceEventRepository;
        this.recommendationService = recommendationService;
        this.recommendationTaskExecutor = recommendationTaskExecutor;
        this.arbitAiRestClient = restClientBuilder
                .baseUrl(arbitAiProperties.baseUrl())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PreferenceCategoriesResponse> getPreferenceCategories() {
        int rand = ThreadLocalRandom.current().nextInt(RANDOM_STATE_BOUND);

        try {
            SeedEventsResponse response = arbitAiRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/seed-events")
                            .queryParam("sample_size", SEED_EVENT_SAMPLE_SIZE)
                            .queryParam("random_state", rand)
                            .build())
                    .retrieve()
                    .body(SeedEventsResponse.class);

            if (response == null || response.events() == null || response.events().size() != SEED_EVENT_SAMPLE_SIZE) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to load seed events.");
            }

            Map<UUID, Event> localEvents = findLocalEvents(response.events());
            long distinctSeedEventCount = response.events().stream()
                    .map(SeedEvent::eventId)
                    .distinct()
                    .count();
            if (localEvents.size() != distinctSeedEventCount) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Seed events did not match local events.");
            }

            return response.events().stream()
                    .map(event -> toPreferenceCategoriesResponse(event, localEvents.get(event.eventId())))
                    .toList();
        } catch (RestClientException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to load seed events.");
        }
    }

    @Transactional
    public void createPreferences(UUID userId, List<UUID> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        List<UUID> requestedEventIds = List.copyOf(eventIds);
        savePreferenceEvents(user, requestedEventIds);
        recommendationTaskExecutor.execute(() -> {
            try {
                recommendationService.createRecommendations(userId, requestedEventIds);
            } catch (RuntimeException exception) {
                log.error("Recommendation background task failed. userId={}, eventIds={}",
                        userId, requestedEventIds, exception);
            }
        });
    }

    private Map<UUID, Event> findLocalEvents(List<SeedEvent> seedEvents) {
        List<UUID> eventIds = seedEvents.stream()
                .map(SeedEvent::eventId)
                .distinct()
                .toList();

        return eventRepository.findAllById(eventIds).stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        Function.identity(),
                        (first, ignored) -> first
                ));
    }

    private PreferenceCategoriesResponse toPreferenceCategoriesResponse(SeedEvent seedEvent, Event localEvent) {
        return new PreferenceCategoriesResponse(
                seedEvent.eventId(),
                seedEvent.title(),
                seedEvent.genre(),
                localEvent.getPosterImageUrl()
        );
    }

    private void savePreferenceEvents(User user, List<UUID> eventIds) {
        userPreferenceEventRepository.deleteAllByUserId(user.getId());
        List<UserPreferenceEvent> preferenceEvents = eventIds.stream()
                .distinct()
                .map(eventId -> UserPreferenceEvent.builder()
                        .user(user)
                        .eventId(eventId)
                        .build())
                .toList();
        userPreferenceEventRepository.saveAll(preferenceEvents);
    }

    private record SeedEventsResponse(
            @JsonProperty("sample_size") int sampleSize,
            String message,
            List<SeedEvent> events
    ) {
    }

    private record SeedEvent(
            @JsonProperty("event_id") UUID eventId,
            String title,
            String genre
    ) {
    }
}
