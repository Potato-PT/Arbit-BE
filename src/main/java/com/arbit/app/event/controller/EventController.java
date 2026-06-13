package com.arbit.app.event.controller;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.event.dto.EventDetailResponse;
import com.arbit.app.event.dto.MatchedEventResponse;
import com.arbit.app.event.dto.EventResponse;
import com.arbit.app.event.dto.EventSearchResultsResponse;
import com.arbit.app.event.dto.EventSearchSort;
import com.arbit.app.event.dto.EventSearchSuggestionsResponse;
import com.arbit.app.event.dto.EventSearchTarget;
import com.arbit.app.event.entity.EventStatus;
import com.arbit.app.event.service.EventSearchService;
import com.arbit.app.event.service.EventService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Event lookup and search APIs")
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final EventService eventService;
    private final EventSearchService eventSearchService;

    public EventController(EventService eventService, EventSearchService eventSearchService) {
        this.eventService = eventService;
        this.eventSearchService = eventSearchService;
    }

    @GetMapping("/{eventId}")
    @Operation(
            summary = "Get event detail",
            description = """
                    Returns the full detail of a single event by its ID.
                    Use this endpoint after the user selects an item from search suggestions or results.
                    """,
            parameters = @Parameter(
                    name = "eventId",
                    in = ParameterIn.PATH,
                    description = "Event UUID",
                    required = true,
                    schema = @Schema(type = "string", format = "uuid"),
                    example = "550e8400-e29b-41d4-a716-446655440000"
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Event detail retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EventDetailApiResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Event not found")
            }
    )
    public ApiResponse<EventDetailResponse> getEventDetail(
            @PathVariable UUID eventId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(eventService.getEventDetail(eventId, userDetails));
    }

    @GetMapping
    @Operation(
            summary = "Get event list",
            description = """
                    Returns a filtered and sorted list of events for the event listing screen.
                    This endpoint is separate from keyword search.
                    """,
            parameters = {
                    @Parameter(name = "category", in = ParameterIn.QUERY, description = "Single category filter."),
                    @Parameter(
                            name = "district",
                            in = ParameterIn.QUERY,
                            description = "District filters. Multiple values allowed by repeating the query parameter.",
                            array = @ArraySchema(schema = @Schema(type = "string"))
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
                            description = "Include only events whose endDate is on or before this date.",
                            schema = @Schema(type = "string", format = "date"),
                            example = "2026-06-30"
                    ),
                    @Parameter(
                            name = "status",
                            in = ParameterIn.QUERY,
                            description = """
                                    Event status filters. Multiple values are allowed by repeating the query parameter.
                                    When omitted, events of every status are returned.
                                    """,
                            array = @ArraySchema(schema = @Schema(type = "string", allowableValues = {"ONGOING", "UPCOMING"}))
                    ),
                    @Parameter(
                            name = "is_free",
                            in = ParameterIn.QUERY,
                            description = """
                                    Free/paid filters. Multiple values are allowed by repeating the query parameter.
                                    When omitted or both true and false are selected, all events are returned.
                                    """,
                            array = @ArraySchema(schema = @Schema(type = "boolean"))
                    ),
                    @Parameter(
                            name = "sort",
                            in = ParameterIn.QUERY,
                            description = """
                                    Sort option. Defaults to deadline when omitted. Only a single lowercase value is
                                    accepted. Empty, repeated, uppercase, or unsupported values return 400.
                                    """,
                            schema = @Schema(type = "string", allowableValues = {"deadline", "latest", "rating"}, defaultValue = "deadline")
                    )
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Event list retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventsApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "sort is empty, repeated, uppercase, or unsupported"
                    )
            }
    )
    public ApiResponse<List<EventResponse>> getEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> district,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "deadline") String sort,
            @Parameter(hidden = true) @RequestParam(required = false) List<EventStatus> status,
            @Parameter(hidden = true) @RequestParam(name = "is_free", required = false) List<Boolean> isFree) {
        log.info("Event list request received. category={}, district={}, startDate={}, endDate={}, sort={}, status={}, isFree={}",
                category, district, startDate, endDate, sort, status, isFree);
        return ApiResponse.success(eventService.getEvents(category, district, startDate, endDate, sort, status, isFree));
    }

    @GetMapping("/matches")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get matched event list",
            description = """
                    Returns the authenticated user's stored personalized recommendations ordered by match score
                    descending. Supports the same category, district, date, and status filters as GET /api/events.
                    """,
            parameters = {
                    @Parameter(name = "category", in = ParameterIn.QUERY, description = "Single category filter."),
                    @Parameter(
                            name = "district",
                            in = ParameterIn.QUERY,
                            description = "District filters. Multiple values allowed by repeating the query parameter.",
                            array = @ArraySchema(schema = @Schema(type = "string"))
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
                            description = "Include only events whose endDate is on or before this date.",
                            schema = @Schema(type = "string", format = "date"),
                            example = "2026-06-30"
                    )
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Matched event list retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MatchedEventsApiResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Authentication is required"
                    )
            }
    )
    public ApiResponse<List<MatchedEventResponse>> getMatchedEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> district,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(hidden = true) @RequestParam(defaultValue = "ONGOING") EventStatus status,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(eventService.getMatchedEvents(
                category, district, startDate, endDate, status, userDetails));
    }

    @Hidden
    @GetMapping("/search/suggestions")
    
    @Operation(
                summary = "Event search suggestions",
                description = """
                    Returns lightweight event suggestions for search input autocomplete and preview cards.
                    Use GET /api/events/{eventId} when the user selects a suggestion and needs full detail.
                    """,
                parameters = {
                    @Parameter(
                            name = "keyword",
                            in = ParameterIn.QUERY,
                            description = "Search keyword.",
                            required = true,
                            schema = @Schema(type = "string"),
                            example = "국악위크"
                    ),
                    @Parameter(
                            name = "target",
                            in = ParameterIn.QUERY,
                            description = "Field to search. Defaults to ALL.",
                            schema = @Schema(type = "string", allowableValues = {"ALL", "TITLE", "CATEGORY", "VENUE", "DISTRICT", "KEYWORD"}, defaultValue = "ALL"),
                            example = "ALL"
                    ),
                    @Parameter(
                            name = "limit",
                            in = ParameterIn.QUERY,
                            description = "Maximum suggestions to return. Defaults to 10, max 20.",
                            schema = @Schema(type = "integer", defaultValue = "10", maximum = "20"),
                            example = "10"
                    )
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Search suggestions retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SearchSuggestionsApiResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "keyword": "국악위크",
                                                "target": "ALL",
                                                "suggestions": [
                                                  {
                                                    "event_id": "de4693dd-7196-4298-9bd9-c6c2a597b6b6",
                                                    "title": "[서울남산국악당] 2026 남산 국악위크 [Vocal Space '조각눈']",
                                                    "category": "국악",
                                                    "venue": "서울남산국악당 크라운해태홀",
                                                    "district": "중구",
                                                    "startDate": "2026-06-06",
                                                    "endDate": "2026-06-06",
                                                    "posterImageUrl": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=3f3433c726c34bfbbca16ea200f7cb86&thumb=Y",
                                                    "price": "전석 30,000원",
                                                    "free": false,
                                                    "status": "UPCOMING",
                                                    "matchedField": "TITLE",
                                                    "highlightText": "[서울남산국악당] 2026 남산 국악위크 [Vocal Space '조각눈']"
                                                  }
                                                ]
                                              },
                                              "error": null
                                            }
                                            """)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
            }
    )
    public ApiResponse<EventSearchSuggestionsResponse> suggestEvents(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "ALL") EventSearchTarget target,
            @RequestParam(required = false) Integer limit) {
        return ApiResponse.success(eventSearchService.getSuggestions(keyword, target, limit));
    }

    @GetMapping("/search")
    @Operation(
            summary = "Event search results",
            description = """
                    Returns paged event search results when the user submits a search.
                    All filters are optional except lat and lng, which are required when sort=distance.
                    """,
            parameters = {
                    @Parameter(name = "keyword", in = ParameterIn.QUERY, description = "Search keyword.", example = "악뮤"),
                    @Parameter(
                            name = "target",
                            in = ParameterIn.QUERY,
                            description = "Field to search. Defaults to ALL.",
                            schema = @Schema(type = "string", allowableValues = {"ALL", "TITLE", "CATEGORY", "VENUE", "DISTRICT", "KEYWORD"}, defaultValue = "ALL"),
                            example = "ALL"
                    ),
                    @Parameter(name = "category", in = ParameterIn.QUERY, description = "Single category filter.", example = "콘서트"),
                    @Parameter(
                            name = "district",
                            in = ParameterIn.QUERY,
                            description = "District filters. Multiple values allowed by repeating the query parameter.",
                            array = @ArraySchema(schema = @Schema(type = "string")),
                            examples = @ExampleObject(name = "multi", value = "[\"송파구\", \"마포구\"]")
                    ),
                    @Parameter(
                            name = "status",
                            in = ParameterIn.QUERY,
                            description = "Event status filter.",
                            schema = @Schema(type = "string", allowableValues = {"ONGOING", "UPCOMING", "CLOSED"}),
                            example = "ONGOING"
                    ),
                    @Parameter(name = "free", in = ParameterIn.QUERY, description = "true for free events, false for paid events.", example = "false"),
                    @Parameter(
                            name = "sort",
                            in = ParameterIn.QUERY,
                            description = "Sort option. Defaults to deadline.",
                            schema = @Schema(type = "string", allowableValues = {"deadline", "latest", "rating", "distance"}, defaultValue = "deadline"),
                            example = "deadline"
                    ),
                    @Parameter(name = "lat", in = ParameterIn.QUERY, description = "User latitude. Required when sort=distance.", example = "37.5665"),
                    @Parameter(name = "lng", in = ParameterIn.QUERY, description = "User longitude. Required when sort=distance.", example = "126.9780"),
                    @Parameter(name = "page", in = ParameterIn.QUERY, description = "Page number. Defaults to 0.", example = "0"),
                    @Parameter(name = "size", in = ParameterIn.QUERY, description = "Page size. Defaults to 20.", example = "20")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Search result retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SearchEventsApiResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "keyword": "악뮤",
                                                "target": "ALL",
                                                "page": 0,
                                                "size": 20,
                                                "totalElements": 1,
                                                "totalPages": 1,
                                                "items": [
                                                  {
                                                    "event_id": "550e8400-e29b-41d4-a716-446655440000",
                                                    "title": "악뮤 콘서트 LOVE EPISODE",
                                                    "category": "콘서트",
                                                    "posterImageUrl": "https://cdn.arbit.app/events/akmu/poster.jpg",
                                                    "venue": "올림픽공원",
                                                    "district": "송파구",
                                                    "startDate": "2026-06-03",
                                                    "endDate": "2026-06-30",
                                                    "free": false,
                                                    "price": "18,600원",
                                                    "status": "ONGOING",
                                                    "averageRating": 4.7
                                                  }
                                                ]
                                              },
                                              "error": null
                                            }
                                            """)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
            }
    )
    public ApiResponse<EventSearchResultsResponse> searchEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "ALL") EventSearchTarget target,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> district,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) Boolean free,
            @RequestParam(defaultValue = "deadline") EventSearchSort sort,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return ApiResponse.success(eventSearchService.search(
                keyword, target, category, district, status, free, sort, lat, lng, page, size));
    }

    @Schema(description = "Wrapped event list response")
    private record EventsApiResponse(
            boolean success,
            @ArraySchema(schema = @Schema(implementation = EventResponse.class))
            List<EventResponse> data,
            Object error
    ) {
    }

    @Schema(description = "Wrapped matched event list response")
    private record MatchedEventsApiResponse(
            boolean success,
            @ArraySchema(schema = @Schema(implementation = MatchedEventResponse.class))
            List<MatchedEventResponse> data,
            Object error
    ) {
    }

    @Schema(description = "Wrapped event detail response")
    private record EventDetailApiResponse(
            boolean success,
            @Schema(implementation = EventDetailResponse.class)
            EventDetailResponse data,
            Object error
    ) {
    }

    @Schema(description = "Wrapped event search suggestions response")
    private record SearchSuggestionsApiResponse(
            boolean success,
            @Schema(implementation = EventSearchSuggestionsResponse.class)
            EventSearchSuggestionsResponse data,
            Object error
    ) {
    }

    @Schema(description = "Wrapped event search response")
    private record SearchEventsApiResponse(
            boolean success,
            @Schema(implementation = EventSearchResultsResponse.class)
            EventSearchResultsResponse data,
            Object error
    ) {
    }
}
