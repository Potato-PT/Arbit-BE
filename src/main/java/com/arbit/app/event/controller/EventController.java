package com.arbit.app.event.controller;

import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.event.dto.EventResponse;
import com.arbit.app.event.entity.EventStatus;
import com.arbit.app.event.repository.EventRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository eventRepository;

    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping
    public ApiResponse<List<EventResponse>> getEvents(
            @RequestParam(defaultValue = "ONGOING") EventStatus status) {
        List<EventResponse> events = eventRepository.findByStatusOrderByEndDateAsc(status).stream()
                .map(EventResponse::from)
                .toList();
        return ApiResponse.success(events);
    }
}
