package com.arbit.app.preference.controller;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.preference.dto.PreferenceCategoriesResponse;
import com.arbit.app.preference.service.PreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Preferences", description = "APIs for choosing categories and taste keywords.")
public class PreferenceController {

    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping("/api/preferences/categories")
    @Operation(
            summary = "Get seed events for choosing preferences",
            description = "Generates a random state, calls the seed-event service, and returns 10 events with event_id, title, genre, and posterImage.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Seed events retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PreferenceCategoriesApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": [
                                                        {
                                                          "event_id": 1,
                                                          "title": "Seoul Media Art Exhibition",
                                                          "genre": "exhibition",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": 2,
                                                          "title": "Modern Art Showcase",
                                                          "genre": "exhibition",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": 3,
                                                          "title": "Indie Band Live",
                                                          "genre": "concert",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": 4,
                                                          "title": "Classical Night",
                                                          "genre": "concert",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": 5,
                                                          "title": "Theater Festival",
                                                          "genre": "performance",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": 6,
                                                          "title": "Dance Performance",
                                                          "genre": "performance",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": 7,
                                                          "title": "Design Fair",
                                                          "genre": "exhibition",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": 8,
                                                          "title": "Jazz Evening",
                                                          "genre": "concert",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": 9,
                                                          "title": "Family Musical",
                                                          "genre": "performance",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": 10,
                                                          "title": "Photo Archive",
                                                          "genre": "exhibition",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        }
                                                      ],
                                                      "error": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public ApiResponse<List<PreferenceCategoriesResponse>> getPreferenceCategories() {
        return ApiResponse.success(preferenceService.getPreferenceCategories());
    }

    @PostMapping("/api/preferences")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Save preferences",
            description = "Receives the authenticated user's selected event identifiers after signup.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(type = "integer", format = "int64", example = "1")),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "data": [1, 3, 7],
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Selected event identifiers received successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CreatePreferenceApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": [1, 3, 7],
                                                      "error": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required")
            }
    )
    public ApiResponse<List<Long>> createPreferences(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody @NotNull @NotEmpty List<@NotNull @Positive Long> eventIds) {
        preferenceService.createPreferences(userDetails.id(), eventIds);
        return ApiResponse.success(eventIds);
    }

    private record PreferenceCategoriesApiResponse(
            boolean success,
            @ArraySchema(schema = @Schema(implementation = PreferenceCategoriesResponse.class))
            List<PreferenceCategoriesResponse> data,
            Object error
    ) {
    }

    private record CreatePreferenceApiResponse(
            boolean success,
            @ArraySchema(schema = @Schema(type = "integer", format = "int64", example = "1"))
            List<Long> data,
            Object error
    ) {
    }
}
