package com.arbit.app.home.service;

import com.arbit.app.event.repository.EventRepository;
import com.arbit.app.event.entity.EventStatus;
import com.arbit.app.home.dto.HomeEventResponse;
import com.arbit.app.home.dto.HomeResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HomeService {

    private static final Logger log = LoggerFactory.getLogger(HomeService.class);

    private final EventRepository eventRepository;

    public HomeService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public HomeResponse getHome() {
        return getHome("-");
    }

    @Transactional(readOnly = true)
    public HomeResponse getHome(String requestId) {
        long startedAt = System.nanoTime();

        log.info(
                "home.repository.call requestId={} repository={} method={} excludedStatus={}",
                requestId,
                EventRepository.class.getSimpleName(),
                "findByStatusNotOrderByCreatedAtDesc",
                EventStatus.CLOSED
        );

        List<HomeEventResponse> events = eventRepository.findByStatusNotOrderByCreatedAtDesc(EventStatus.CLOSED).stream()
                .peek(event -> log.debug(
                        "home.repository.row requestId={} eventId={} title={} status={} category={}",
                        requestId,
                        event.getId(),
                        event.getTitle(),
                        event.getStatus(),
                        event.getCategory().getName()
                ))
                .map(HomeEventResponse::from)
                .toList();

        log.info(
                "home.repository.return requestId={} eventCount={} elapsedMs={}",
                requestId,
                events.size(),
                elapsedMillis(startedAt)
        );

        log.info("home.dto.create requestId={} eventCount={}", requestId, events.size());
        return new HomeResponse(events);
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
