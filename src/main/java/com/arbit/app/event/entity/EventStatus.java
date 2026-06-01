package com.arbit.app.event.entity;

import java.time.LocalDate;
import java.time.ZoneId;

public enum EventStatus {
    UPCOMING,
    ONGOING,
    CLOSED;

    private static final ZoneId STATUS_ZONE = ZoneId.of("Asia/Seoul");

    public static LocalDate today() {
        return LocalDate.now(STATUS_ZONE);
    }

    public static EventStatus from(LocalDate startDate, LocalDate endDate, LocalDate today) {
        if (startDate.isAfter(today)) {
            return UPCOMING;
        }
        if (endDate.isBefore(today)) {
            return CLOSED;
        }
        return ONGOING;
    }
}
