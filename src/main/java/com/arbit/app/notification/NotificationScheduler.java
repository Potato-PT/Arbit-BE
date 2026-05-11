package com.arbit.app.notification;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void sendDailyNotifications() {
        // Notification targets will include bookmarked, upcoming, and closing-soon events.
    }
}
