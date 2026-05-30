package com.arbit.app.home.service;

import com.arbit.app.event.repository.EventRepository;
import com.arbit.app.event.entity.EventStatus;
import com.arbit.app.home.dto.HomeEventResponse;
import com.arbit.app.home.dto.HomeResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HomeService {

    private final EventRepository eventRepository;

    public HomeService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public HomeResponse getHome() {
        List<HomeEventResponse> events = eventRepository.findByStatusNotOrderByCreatedAtDesc(EventStatus.CLOSED).stream()
                .map(HomeEventResponse::from)
                .toList();

        return new HomeResponse(events);
    }
}
