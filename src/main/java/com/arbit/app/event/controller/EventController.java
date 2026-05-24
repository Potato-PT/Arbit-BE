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

    @GetMapping
    @Operation(
            summary = "이벤트 목록 조회",
            description = """
                    Returns a filtered and sorted list of events for the 'View the entire performance' screen.
                    All filter parameters are optional.
                    When multiple filters are provided, only events matching all conditions are returned with AND logic.
                    This endpoint does not have a search function.

                    Filters:
                    - category: single genre only
                    - district: multiple borough values allowed
                    - startDate: include only events whose startDate is on or after this date
                    - endDate: include only events whose startDate is on or before this date

                    Sorting:
                    - match: preference match score descending, falls back to deadline for unauthenticated requests
                    - deadline: endDate ascending
                    - latest: startDate descending
                    - rating: average user rating descending, unrated events last

                    status is computed server-side:
                    - ONGOING: startDate <= today <= endDate
                    - UPCOMING: startDate > today

                    bookmarked reflects the authenticated user's bookmark state.
                    For unauthenticated requests, bookmarked is always false.
                    """,
            parameters = {
                    @Parameter(
                            name = "category",
                            in = ParameterIn.QUERY,
                            description = "Single genre only, for example 전시, 공연, 행사.",
                            schema = @Schema(type = "string"),
                            example = "전시"
                    ),
                    @Parameter(
                            name = "district",
                            in = ParameterIn.QUERY,
                            description = "Borough-level region. Multiple values allowed by repeating the query parameter.",
                            array = @ArraySchema(schema = @Schema(type = "string")),
                            examples = @ExampleObject(name = "multi", value = "[\"Mapo-gu\", \"Jongno-gu\"]")
                    ),
                    @Parameter(
                            name = "startDate",
                            in = ParameterIn.QUERY,
                            description = "Include only events whose startDate is on or after this date.",
                            schema = @Schema(type = "string", format = "date"),
                            example = "2026-06-01"
                    ),
                    @Parameter(
                            name = "endDate",
                            in = ParameterIn.QUERY,
                            description = "Include only events whose startDate is on or before this date.",
                            schema = @Schema(type = "string", format = "date"),
                            example = "2026-06-30"
                    ),
                    @Parameter(
                            name = "sort",
                            in = ParameterIn.QUERY,
                            description = "Determines the order of results. Single value only. Defaults to deadline.",
                            schema = @Schema(type = "string", allowableValues = {"match", "deadline", "latest", "rating"}, defaultValue = "deadline"),
                            example = "deadline"
                    )
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Filtered and sorted event list retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EventsApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": [
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
                                                          "rating": 4.1,
                                                          "bookmarked": true,
                                                          "createdAt": "2026-05-01"
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
    public ApiResponse<List<EventResponse>> getEvents(
            @Parameter(hidden = true)
            @RequestParam(defaultValue = "ONGOING") EventStatus status) {
        List<EventResponse> events = eventRepository.findByStatusOrderByEndDateAsc(status).stream()
                .map(EventResponse::from)
                .toList();
        return ApiResponse.success(events);
    }

    @GetMapping("/search")
    @Operation(
            summary = "이벤트 검색",
            description = """
                    Returns a filtered and sorted list of events.
                    All filter parameters are optional.
                    When multiple filters are provided, only events matching all conditions are returned with AND logic.
                    The frontend accumulates selected filters and re-requests this endpoint each time the user changes a filter option.

                    Query parameters:
                    - title: search keyword matched against event title
                    - category: single genre only
                    - district: borough-level region, multiple values allowed
                    - status: ONGOING or UPCOMING, multiple values allowed
                    - free: true for free events, false for paid, multiple values allowed
                    - sort: distance, deadline, or upcoming. Default is deadline
                    - lat: user latitude, required when sort=distance
                    - lng: user longitude, required when sort=distance

                    Sort behavior:
                    - deadline: endDate ascending
                    - upcoming: startDate ascending
                    - distance: calculated distance from user's coordinates ascending

                    status is computed server-side:
                    - ONGOING: startDate <= today <= endDate
                    - UPCOMING: startDate > today

                    distance is computed server-side when lat and lng are provided; otherwise null.
                    bookmarked reflects the authenticated user's bookmark state.
                    For unauthenticated requests, bookmarked is always false.
                    """,
            parameters = {
                    @Parameter(
                            name = "title",
                            in = ParameterIn.QUERY,
                            description = "Search keyword matched against event title. Single value only.",
                            schema = @Schema(type = "string"),
                            example = "모네"
                    ),
                    @Parameter(
                            name = "category",
                            in = ParameterIn.QUERY,
                            description = "Single genre only, for example 전시, 공연, 행사.",
                            schema = @Schema(type = "string"),
                            example = "전시"
                    ),
                    @Parameter(
                            name = "district",
                            in = ParameterIn.QUERY,
                            description = "Borough-level region. Multiple values allowed by repeating the query parameter.",
                            array = @ArraySchema(schema = @Schema(type = "string")),
                            examples = @ExampleObject(name = "multi", value = "[\"Mapo-gu\", \"Jongno-gu\"]")
                    ),
                    @Parameter(
                            name = "status",
                            in = ParameterIn.QUERY,
                            description = "Allowed values: ONGOING or UPCOMING. Multiple values allowed by repeating the query parameter.",
                            array = @ArraySchema(schema = @Schema(type = "string", allowableValues = {"ONGOING", "UPCOMING"})),
                            examples = @ExampleObject(name = "multi", value = "[\"ONGOING\", \"UPCOMING\"]")
                    ),
                    @Parameter(
                            name = "free",
                            in = ParameterIn.QUERY,
                            description = "true for free events, false for paid. Multiple values allowed by repeating the query parameter.",
                            array = @ArraySchema(schema = @Schema(type = "boolean")),
                            examples = @ExampleObject(name = "multi", value = "[true, false]")
                    ),
                    @Parameter(
                            name = "sort",
                            in = ParameterIn.QUERY,
                            description = "Sort option. Single value only. Default is deadline.",
                            schema = @Schema(type = "string", allowableValues = {"distance", "deadline", "upcoming"}, defaultValue = "deadline"),
                            example = "deadline"
                    ),
                    @Parameter(
                            name = "lat",
                            in = ParameterIn.QUERY,
                            description = "User latitude. Required when sort=distance.",
                            schema = @Schema(type = "number", format = "float"),
                            example = "37.5665"
                    ),
                    @Parameter(
                            name = "lng",
                            in = ParameterIn.QUERY,
                            description = "User longitude. Required when sort=distance.",
                            schema = @Schema(type = "number", format = "float"),
                            example = "126.9780"
                    )
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Search result retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SearchEventsApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": [
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
                                    )
                            )
                    )
            }
    )
    public ApiResponse<List<EventResponse>> searchEvents(
            @Parameter(hidden = true)
            @RequestParam(defaultValue = "ONGOING") EventStatus status) {
        List<EventResponse> events = eventRepository.findByStatusOrderByEndDateAsc(status).stream()
                .map(EventResponse::from)
                .toList();
        return ApiResponse.success(events);
    }

    @Schema(description = "Wrapped event list response")
    private record EventsApiResponse(
            boolean success,
            @ArraySchema(schema = @Schema(implementation = EventSwaggerItem.class))
            List<EventSwaggerItem> data,
            Object error
    ) {
    }

    @Schema(description = "Wrapped event search response")
    private record SearchEventsApiResponse(
            boolean success,
            @ArraySchema(schema = @Schema(implementation = SearchEventSwaggerItem.class))
            List<SearchEventSwaggerItem> data,
            Object error
    ) {
    }

    @Schema(description = "Event item used in list results")
    private record EventSwaggerItem(
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
            Double rating,
            boolean bookmarked,
            LocalDate createdAt
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
