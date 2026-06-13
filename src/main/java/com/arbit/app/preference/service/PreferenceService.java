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
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class PreferenceService {

    private static final int SEED_EVENT_SAMPLE_SIZE = 20;
    private static final int RANDOM_STATE_BOUND = 1_000_000;
    private static final int MIN_PREFERENCE_EVENT_COUNT = 5;
    private static final int MAX_PREFERENCE_EVENT_COUNT = 20;

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final UserPreferenceEventRepository userPreferenceEventRepository;
    private final PreferenceRecommendationService recommendationService;
    private final RestClient arbitAiRestClient;

    public PreferenceService(UserRepository userRepository,
                             EventRepository eventRepository,
                             UserPreferenceEventRepository userPreferenceEventRepository,
                             PreferenceRecommendationService recommendationService,
                             RestClient.Builder restClientBuilder,
                             ArbitAiProperties arbitAiProperties) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.userPreferenceEventRepository = userPreferenceEventRepository;
        this.recommendationService = recommendationService;
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
                    .map(this::toPreferenceCategoriesResponse)
                    .toList();
        } catch (RestClientException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to load seed events.");
        }
    }

    @Transactional
    public void createPreferences(UUID userId, List<UUID> eventIds) {
        validatePreferenceEventIds(eventIds);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        List<UUID> requestedEventIds = List.copyOf(eventIds);
        savePreferenceEvents(user, requestedEventIds);
        recommendationService.createRecommendations(userId, requestedEventIds);
    }

    private void validatePreferenceEventIds(List<UUID> eventIds) {
        if (eventIds == null
                || eventIds.size() < MIN_PREFERENCE_EVENT_COUNT
                || eventIds.size() > MAX_PREFERENCE_EVENT_COUNT
                || eventIds.stream().anyMatch(Objects::isNull)
                || eventIds.stream().distinct().count() != eventIds.size()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "Select between 5 and 20 distinct events."
            );
        }
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

    private PreferenceCategoriesResponse toPreferenceCategoriesResponse(SeedEvent seedEvent) {
        return new PreferenceCategoriesResponse(
                seedEvent.eventId(),
                seedEvent.title(),
                seedEvent.genre(),
                seedEvent.imageUrl()
        );
    }

    private void savePreferenceEvents(User user, List<UUID> eventIds) {
        userPreferenceEventRepository.deleteAllByUserId(user.getId());
        userPreferenceEventRepository.flush();
        List<UserPreferenceEvent> preferenceEvents = eventIds.stream()
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
            @JsonProperty("image_url") String imageUrl,
            String title,
            String genre
    ) {
    }
}
