package com.arbit.app.event.scheduler;

import com.arbit.app.event.service.EventStatusRefreshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EventStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventStatusScheduler.class);

    private final EventStatusRefreshService eventStatusRefreshService;

    public EventStatusScheduler(EventStatusRefreshService eventStatusRefreshService) {
        this.eventStatusRefreshService = eventStatusRefreshService;
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void refreshEventStatusesAtMidnight() {
        int updatedCount = eventStatusRefreshService.refreshAllEventStatuses();
        log.info("Event status refresh completed. updatedCount={}", updatedCount);
    }
}
