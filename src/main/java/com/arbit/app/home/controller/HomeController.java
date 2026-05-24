package com.arbit.app.home.controller;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.common.response.ErrorResponse;
import com.arbit.app.home.dto.HomeResponse;
import com.arbit.app.home.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@Tag(name = "Home", description = "Home screen APIs for the authenticated user.")
@SecurityRequirement(name = "bearerAuth")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping
    @Operation(
            summary = "Get home screen data",
            description = "Returns the authenticated user's recommended events.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Home screen data retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = HomeApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": {
                                                        "recommendedEvents": [
                                                          {
                                                            "event": {
                                                              "id": "11111111-1111-1111-1111-111111111111",
                                                              "title": "Echoes of Silence",
                                                              "category": "Media Art",
                                                              "posterImageUrl": "https://cdn.arbit.app/events/light-museum/poster.jpg",
                                                              "venue": "Metropolitan Museum",
                                                              "district": "Jongno-gu",
                                                              "startDate": "2026-05-01",
                                                              "endDate": "2026-06-30",
                                                              "free": false,
                                                              "status": "ONGOING"
                                                            }
                                                          }
                                                        ]
                                                      },
                                                      "error": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Authentication is required"
                    )
            }
    )
    public ApiResponse<HomeResponse> getHome(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(homeService.getHome(userDetails.id()));
    }

    @Schema(description = "Wrapped home response")
    private record HomeApiResponse(
            boolean success,
            HomeResponse data,
            ErrorResponse error
    ) {
    }
}
