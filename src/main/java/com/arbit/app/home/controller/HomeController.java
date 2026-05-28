package com.arbit.app.home.controller;

import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.common.response.ErrorResponse;
import com.arbit.app.home.dto.HomeResponse;
import com.arbit.app.home.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@Tag(name = "Home", description = "Home screen APIs.")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping
    @Operation(
            summary = "Get home screen data",
            description = "Returns the latest registered events for non-logged-in users.",
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
                                                        "events": [
                                                          {
                                                            "title": "Echoes of Silence",
                                                            "category": "Media Art",
                                                            "posterImageUrl": "https://cdn.arbit.app/events/light-museum/poster.jpg",
                                                            "url": "전시/공연 홈페이지 주소",
                                                            "venue": "Metropolitan Museum",
                                                            "startDate": "2026-05-01",
                                                            "endDate": "2026-06-30",
                                                            "free": false,
                                                            "status": "ONGOING"
                                                          }
                                                        ]
                                                      },
                                                      "error": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public ApiResponse<HomeResponse> getHome() {
        return ApiResponse.success(homeService.getHome());
    }

    @Schema(description = "Wrapped home response")
    private record HomeApiResponse(
            boolean success,
            HomeSwaggerResponse data,
            ErrorResponse error
    ) {
    }

    @Schema(description = "Home screen response")
    private record HomeSwaggerResponse(
            List<HomeEventSwaggerItem> events
    ) {
    }

    @Schema(description = "Home event item")
    private record HomeEventSwaggerItem(
            String title,
            String category,
            String posterImageUrl,
            String url,
            String venue,
            LocalDate startDate,
            LocalDate endDate,
            boolean free,
            String status
    ) {
    }
}
