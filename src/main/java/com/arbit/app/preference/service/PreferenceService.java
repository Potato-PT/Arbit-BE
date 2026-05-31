package com.arbit.app.preference.service;

import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import com.arbit.app.preference.dto.PreferenceCategoriesResponse;
import com.arbit.app.user.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class PreferenceService {

    private static final int SEED_EVENT_SAMPLE_SIZE = 10;
    private static final int RANDOM_STATE_BOUND = 1_000_000;
    private static final String DEFAULT_POSTER_IMAGE_URL =
            "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png";

    private final UserRepository userRepository;
    private final PreferenceRecommendationAsyncService recommendationAsyncService;
    private final RestClient arbitAiRestClient;

    public PreferenceService(UserRepository userRepository,
                             PreferenceRecommendationAsyncService recommendationAsyncService,
                             RestClient.Builder restClientBuilder,
                             ArbitAiProperties arbitAiProperties) {
        this.userRepository = userRepository;
        this.recommendationAsyncService = recommendationAsyncService;
        this.arbitAiRestClient = restClientBuilder
                .baseUrl(arbitAiProperties.baseUrl())
                .build();
    }

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

            return response.events().stream()
                    .map(event -> new PreferenceCategoriesResponse(
                            event.eventId(),
                            event.title(),
                            event.genre(),
                            DEFAULT_POSTER_IMAGE_URL
                    ))
                    .toList();
        } catch (RestClientException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to load seed events.");
        }
    }

    public void createPreferences(UUID userId, List<UUID> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        recommendationAsyncService.createRecommendations(userId, List.copyOf(eventIds));
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
