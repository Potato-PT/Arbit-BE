package com.arbit.app.event.controller;

import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.event.dto.EventResponse;
import com.arbit.app.event.entity.EventStatus;
import com.arbit.app.event.repository.EventRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@Tag(name = "이벤트", description = "전시, 공연, 행사 검색 및 조회 API")
public class EventController {

    private final EventRepository eventRepository;

    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping("/search")
    @Operation(
            summary = "이벤트 검색",
            description = """
                    Documents the frontend event search specification for `GET /events/search`.
                    All filter parameters are optional.
                    When multiple filters are provided, only events matching all conditions are returned with AND logic.
                    The frontend accumulates selected filters and re-requests this endpoint whenever filters change.
                    Sort options:
                    - deadline: endDate ascending
                    - upcoming: startDate ascending
                    - distance: calculated distance ascending, requires lat and lng
                    status is computed server-side:
                    - ONGOING: startDate <= today <= endDate
                    - UPCOMING: startDate > today
                    distance is returned when lat and lng are provided; otherwise null.
                    bookmarked reflects the authenticated user's bookmark state; for unauthenticated requests it is always false.
                    """,
            parameters = {
                    @Parameter(
                            name = "title",
                            in = ParameterIn.QUERY,
                            description = "Search keyword matched against event title",
                            schema = @Schema(type = "string"),
                            example = "모네"
                    ),
                    @Parameter(
                            name = "category",
                            in = ParameterIn.QUERY,
                            description = "Single genre only, for example 뮤지컬, 연극",
                            schema = @Schema(type = "string"),
                            example = "뮤지컬"
                    ),
                    @Parameter(
                            name = "district",
                            in = ParameterIn.QUERY,
                            description = "Borough-level region. Repeat the query parameter for multi-select.",
                            array = @ArraySchema(schema = @Schema(type = "string")),
                            example = "[\"Mapo-gu\", \"Jongno-gu\"]"
                    ),
                    @Parameter(
                            name = "status",
                            in = ParameterIn.QUERY,
                            description = "Repeat the query parameter for multi-select. Allowed values: ONGOING, UPCOMING",
                            array = @ArraySchema(schema = @Schema(type = "string", allowableValues = {"ONGOING", "UPCOMING"})),
                            example = "[\"ONGOING\", \"UPCOMING\"]"
                    ),
                    @Parameter(
                            name = "free",
                            in = ParameterIn.QUERY,
                            description = "Repeat the query parameter for multi-select. true for free events, false for paid",
                            array = @ArraySchema(schema = @Schema(type = "boolean")),
                            example = "[true, false]"
                    ),
                    @Parameter(
                            name = "sort",
                            in = ParameterIn.QUERY,
                            description = "Sort option. Default is deadline",
                            schema = @Schema(type = "string", allowableValues = {"distance", "deadline", "upcoming"}, defaultValue = "deadline"),
                            example = "deadline"
                    ),
                    @Parameter(
                            name = "lat",
                            in = ParameterIn.QUERY,
                            description = "User latitude. Required when sort=distance",
                            schema = @Schema(type = "number", format = "float"),
                            example = "37.5665"
                    ),
                    @Parameter(
                            name = "lng",
                            in = ParameterIn.QUERY,
                            description = "User longitude. Required when sort=distance",
                            schema = @Schema(type = "number", format = "float"),
                            example = "126.9780"
                    )
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Filtered and sorted event list retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SearchEventsApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "response",
                                                    value = """
                                                            {
                                                              "success": true,
                                                              "events": [
                                                                {
                                                                  "title": "Echoes of Silence",
                                                                  "category": "Media Art",
                                                                  "posterImageUrl": "https://cdn.arbit.app/events/light-museum/poster.jpg",
                                                                  "url": "https://example.com/events/echoes-of-silence",
                                                                  "district": "Jongno-gu",
                                                                  "venue": "Metropolitan Museum",
                                                                  "startDate": "2026-05-01",
                                                                  "endDate": "2026-06-30",
                                                                  "free": false,
                                                                  "status": "ONGOING",
                                                                  "distance": "1.2km",
                                                                  "bookmarked": true
                                                                }
                                                              ],
                                                              "error": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "request-search-only",
                                                    summary = "Search only",
                                                    value = "GET /events?q=모네"
                                            ),
                                            @ExampleObject(
                                                    name = "request-genre-district-status",
                                                    summary = "Genre + district + status",
                                                    value = "GET /events?genre=전시&district=Mapo-gu&district=Jongno-gu&status=ONGOING"
                                            ),
                                            @ExampleObject(
                                                    name = "request-full-filter",
                                                    summary = "Full filter with sort",
                                                    value = "GET /events?genre=전시&district=Mapo-gu&status=ONGOING&free=true&sort=deadline"
                                            ),
                                            @ExampleObject(
                                                    name = "request-distance",
                                                    summary = "Distance sort",
                                                    value = "GET /events?sort=distance&lat=37.5665&lng=126.9780"
                                            )
                                    }
                            )
                    )
            }
    )
    public ApiResponse<List<EventResponse>> getEvents(
            @Parameter(hidden = true)
            @RequestParam(defaultValue = "ONGOING") EventStatus status) {
        List<EventResponse> events = eventRepository.findByStatusOrderByEndDateAsc(status).stream()
                .map(EventResponse::from)
                .toList();
        return ApiResponse.success(events);
    }

    @Schema(description = "Wrapped event search response")
    private record SearchEventsApiResponse(
            boolean success,
            @ArraySchema(schema = @Schema(implementation = SearchEventSwaggerItem.class))
            List<SearchEventSwaggerItem> events,
            Object error
    ) {
    }

    @Schema(description = "Event item used in search results")
    private record SearchEventSwaggerItem(
            String title,
            String category,
            String posterImageUrl,
            String url,
            String district,
            String venue,
            LocalDate startDate,
            LocalDate endDate,
            boolean free,
            String status,
            String distance,
            boolean bookmarked
    ) {
    }
}
