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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class PreferenceService {

    private static final Logger log = LoggerFactory.getLogger(PreferenceService.class);
    private static final int SEED_EVENT_SAMPLE_SIZE = 20;
    private static final int RANDOM_STATE_BOUND = 1_000_000;
    private static final int MIN_PREFERENCE_EVENT_COUNT = 5;
    private static final int MAX_PREFERENCE_EVENT_COUNT = 20;

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final UserPreferenceEventRepository userPreferenceEventRepository;
    private final PreferenceRecommendationService recommendationService;
    private final RestClient arbitAiRestClient;
    private final String arbitAiBaseUrl;

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
        this.arbitAiBaseUrl = arbitAiProperties.baseUrl();
        this.arbitAiRestClient = restClientBuilder
                .baseUrl(arbitAiBaseUrl)
                .build();
    }

    @Transactional(readOnly = true)
    public List<PreferenceCategoriesResponse> getPreferenceCategories(String requestId) {
        long startedAt = System.nanoTime();
        int rand = ThreadLocalRandom.current().nextInt(RANDOM_STATE_BOUND);
        log.info("preference.seed.prepare requestId={} aiBaseUrl={} sampleSize={} randomState={}",
                requestId, arbitAiBaseUrl, SEED_EVENT_SAMPLE_SIZE, rand);

        try {
            long aiStartedAt = System.nanoTime();
            log.info("preference.seed.ai.request.start requestId={} method=GET path=/seed-events sampleSize={} randomState={}",
                    requestId, SEED_EVENT_SAMPLE_SIZE, rand);

            SeedEventsResponse response = arbitAiRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/seed-events")
                            .queryParam("sample_size", SEED_EVENT_SAMPLE_SIZE)
                            .queryParam("random_state", rand)
                            .build())
                    .retrieve()
                    .body(SeedEventsResponse.class);

            log.info("preference.seed.ai.request.end requestId={} responsePresent={} declaredSampleSize={} eventCount={} elapsedMs={}",
                    requestId,
                    response != null,
                    response == null ? null : response.sampleSize(),
                    response == null || response.events() == null ? null : response.events().size(),
                    elapsedMillis(aiStartedAt));

            if (response == null || response.events() == null || response.events().size() != SEED_EVENT_SAMPLE_SIZE) {
                log.error("preference.seed.ai.response.invalid requestId={} expectedEventCount={} actualEventCount={}",
                        requestId,
                        SEED_EVENT_SAMPLE_SIZE,
                        response == null || response.events() == null ? null : response.events().size());
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to load seed events.");
            }

            List<UUID> eventIds = toEventIds(response.events(), requestId);
            log.info("preference.seed.ai.response.event-ids requestId={} eventIds={}",
                    requestId, eventIds);
            log.info("preference.seed.db.lookup.start requestId={} eventCount={} eventIds={}",
                    requestId, eventIds.size(), eventIds);

            long dbStartedAt = System.nanoTime();
            Map<UUID, Event> localEvents = findLocalEvents(eventIds);
            long distinctSeedEventCount = eventIds.stream().distinct().count();
            log.info("preference.seed.db.lookup.events requestId={} events={}",
                    requestId, toEventLogEntries(eventIds, localEvents));
            log.info("preference.seed.db.lookup.end requestId={} distinctAiEventCount={} matchedLocalEventCount={} elapsedMs={}",
                    requestId, distinctSeedEventCount, localEvents.size(), elapsedMillis(dbStartedAt));

            if (localEvents.size() != distinctSeedEventCount) {
                List<UUID> missingEventIds = eventIds.stream()
                        .distinct()
                        .filter(eventId -> !localEvents.containsKey(eventId))
                        .toList();
                log.error("preference.seed.db.mismatch requestId={} missingEventIds={}", requestId, missingEventIds);
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Seed events did not match local events.");
            }

            List<PreferenceCategoriesResponse> result = response.events().stream()
                    .map(this::toPreferenceCategoriesResponse)
                    .toList();
            log.info("preference.seed.complete requestId={} responseEventCount={} elapsedMs={}",
                    requestId, result.size(), elapsedMillis(startedAt));
            return result;
        } catch (RestClientResponseException exception) {
            log.error("preference.seed.ai.request.failed requestId={} statusCode={} responseBody={} elapsedMs={}",
                    requestId,
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString(),
                    elapsedMillis(startedAt),
                    exception);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to load seed events.");
        } catch (RestClientException | IllegalArgumentException exception) {
            log.error("preference.seed.failed requestId={} exceptionType={} rootCause={} elapsedMs={}",
                    requestId,
                    exception.getClass().getName(),
                    rootCauseMessage(exception),
                    elapsedMillis(startedAt),
                    exception);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to load seed events.");
        } catch (RuntimeException exception) {
            log.error("preference.seed.failed requestId={} elapsedMs={}",
                    requestId, elapsedMillis(startedAt), exception);
            throw exception;
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

    private Map<UUID, Event> findLocalEvents(List<UUID> eventIds) {
        return eventRepository.findAllById(eventIds.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        Function.identity(),
                        (first, ignored) -> first
                ));
    }

    private List<EventLogEntry> toEventLogEntries(List<UUID> eventIds, Map<UUID, Event> localEvents) {
        return eventIds.stream()
                .distinct()
                .map(localEvents::get)
                .filter(Objects::nonNull)
                .map(event -> new EventLogEntry(event.getId(), event.getTitle()))
                .toList();
    }

    private PreferenceCategoriesResponse toPreferenceCategoriesResponse(SeedEvent seedEvent) {
        return new PreferenceCategoriesResponse(
                UUID.fromString(seedEvent.eventId()),
                seedEvent.title(),
                seedEvent.genre(),
                seedEvent.imageUrl()
        );
    }

    private List<UUID> toEventIds(List<SeedEvent> seedEvents, String requestId) {
        List<String> invalidEventIds = seedEvents.stream()
                .map(SeedEvent::eventId)
                .filter(eventId -> !isUuid(eventId))
                .toList();
        if (!invalidEventIds.isEmpty()) {
            log.error("preference.seed.ai.response.invalid-event-ids requestId={} invalidEventIds={}",
                    requestId, invalidEventIds);
            throw new IllegalArgumentException("AI seed event IDs must be UUIDs.");
        }
        return seedEvents.stream()
                .map(SeedEvent::eventId)
                .map(UUID::fromString)
                .toList();
    }

    private boolean isUuid(String value) {
        if (value == null) {
            return false;
        }
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause.getClass().getName() + ": " + rootCause.getMessage();
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
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
            @JsonProperty("event_id") String eventId,
            @JsonProperty("image_url") String imageUrl,
            String title,
            String genre
    ) {
    }

    private record EventLogEntry(UUID eventId, String title) {
    }
}
