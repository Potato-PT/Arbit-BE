package com.arbit.app.event.service;

import com.arbit.app.event.entity.Event;
import com.arbit.app.event.repository.EventRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventStatusRefreshService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final EventRepository eventRepository;
    private final Clock clock;

    @Autowired
    public EventStatusRefreshService(EventRepository eventRepository) {
        this(eventRepository, Clock.system(SEOUL_ZONE));
    }

    EventStatusRefreshService(EventRepository eventRepository, Clock clock) {
        this.eventRepository = eventRepository;
        this.clock = clock;
    }

    @Transactional
    public int refreshAllEventStatuses() {
        LocalDate today = LocalDate.now(clock);
        List<Event> events = eventRepository.findAll();

        int updatedCount = 0;
        for (Event event : events) {
            if (event.updateStatus(today)) {
                updatedCount++;
            }
        }
        return updatedCount;
    }
}
