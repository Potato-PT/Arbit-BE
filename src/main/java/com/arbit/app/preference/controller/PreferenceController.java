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
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "취향 입력", description = "APIs for choosing categories and taste keywords.")
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
                                                          "event_id": "550e8400-e29b-41d4-a716-446655440001",
                                                          "title": "Seoul Media Art Exhibition",
                                                          "genre": "exhibition",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": "550e8400-e29b-41d4-a716-446655440002",
                                                          "title": "Modern Art Showcase",
                                                          "genre": "exhibition",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": "550e8400-e29b-41d4-a716-446655440003",
                                                          "title": "Indie Band Live",
                                                          "genre": "concert",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": "550e8400-e29b-41d4-a716-446655440004",
                                                          "title": "Classical Night",
                                                          "genre": "concert",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": "550e8400-e29b-41d4-a716-446655440005",
                                                          "title": "Theater Festival",
                                                          "genre": "performance",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": "550e8400-e29b-41d4-a716-446655440006",
                                                          "title": "Dance Performance",
                                                          "genre": "performance",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": "550e8400-e29b-41d4-a716-446655440007",
                                                          "title": "Design Fair",
                                                          "genre": "exhibition",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": "550e8400-e29b-41d4-a716-446655440008",
                                                          "title": "Jazz Evening",
                                                          "genre": "concert",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": "550e8400-e29b-41d4-a716-446655440009",
                                                          "title": "Family Musical",
                                                          "genre": "performance",
                                                          "posterImage": "https://storage.googleapis.com/deepflow-image-storage/background-image/image_1.png"
                                                        },
                                                        {
                                                          "event_id": "550e8400-e29b-41d4-a716-446655440010",
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
            summary = "취향 입력",
            description = "Stores the authenticated user's selected categories and taste keywords after signup.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreatePreferenceApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "data": [
                                                "550e8400-e29b-41d4-a716-446655440001",
                                                "550e8400-e29b-41d4-a716-446655440003",
                                                "550e8400-e29b-41d4-a716-446655440007"
                                              ],
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
                                                      "data": [
                                                        "550e8400-e29b-41d4-a716-446655440001",
                                                        "550e8400-e29b-41d4-a716-446655440003",
                                                        "550e8400-e29b-41d4-a716-446655440007"
                                                      ],
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
    public ApiResponse<List<UUID>> createPreferences(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ApiResponse<List<UUID>> request) {
        preferenceService.createPreferences(userDetails.id(), request.data());
        return ApiResponse.success(request.data());
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
            @ArraySchema(schema = @Schema(type = "string", format = "uuid", example = "550e8400-e29b-41d4-a716-446655440001"))
            List<UUID> data,
            Object error
    ) {
    }
}
