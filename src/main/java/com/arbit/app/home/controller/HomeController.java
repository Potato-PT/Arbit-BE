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
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@Tag(name = "Home", description = "Home screen APIs.")
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping
    @Operation(
            summary = "Get home screen data",
            description = "Returns all non-closed events for non-logged-in users, ordered by latest registration.",
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
    public ApiResponse<HomeResponse> getHome(HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long startedAt = System.nanoTime();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String clientIp = request.getRemoteAddr();

        log.info(
                "home.request.start requestId={} method={} uri={} query={} clientIp={}",
                requestId,
                method,
                uri,
                query,
                clientIp
        );

        try {
            log.info("home.service.call requestId={}", requestId);
            HomeResponse home = homeService.getHome(requestId);
            log.info(
                    "home.service.return requestId={} eventCount={}",
                    requestId,
                    home.events().size()
            );

            ApiResponse<HomeResponse> response = ApiResponse.success(home);
            log.info(
                    "home.response.ready requestId={} success={} eventCount={} elapsedMs={}",
                    requestId,
                    response.success(),
                    home.events().size(),
                    elapsedMillis(startedAt)
            );
            return response;
        } catch (RuntimeException exception) {
            log.error(
                    "home.request.error requestId={} method={} uri={} elapsedMs={}",
                    requestId,
                    method,
                    uri,
                    elapsedMillis(startedAt),
                    exception
            );
            throw exception;
        } finally {
            log.info(
                    "home.request.end requestId={} method={} uri={} elapsedMs={}",
                    requestId,
                    method,
                    uri,
                    elapsedMillis(startedAt)
            );
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
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
