package com.arbit.app.event.controller;

import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.event.dto.EventResponse;
import com.arbit.app.event.entity.EventStatus;
import com.arbit.app.event.repository.EventRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@Tag(name = "이벤트", description = "전시, 공연, 행사 정보를 조회합니다.")
public class EventController {

    private final EventRepository eventRepository;

    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping
    @Operation(
            summary = "진행 상태에 맞는 이벤트 목록을 조회합니다.",
            description = "이벤트 상태를 기준으로 전시, 공연, 행사 목록을 마감일 순으로 조회합니다."
    )
    public ApiResponse<List<EventResponse>> getEvents(
            @Parameter(description = "조회할 이벤트 상태")
            @RequestParam(defaultValue = "ONGOING") EventStatus status) {
        List<EventResponse> events = eventRepository.findByStatusOrderByEndDateAsc(status).stream()
                .map(EventResponse::from)
                .toList();
        return ApiResponse.success(events);
    }
}
