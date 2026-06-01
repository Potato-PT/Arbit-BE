package com.arbit.app.event.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EventStatusTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 1);

    @Test
    void fromReturnsUpcomingWhenStartDateIsAfterToday() {
        EventStatus status = EventStatus.from(
                LocalDate.of(2026, 6, 2),
                LocalDate.of(2026, 6, 30),
                TODAY);

        assertThat(status).isEqualTo(EventStatus.UPCOMING);
    }

    @Test
    void fromReturnsClosedWhenEndDateIsBeforeToday() {
        EventStatus status = EventStatus.from(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                TODAY);

        assertThat(status).isEqualTo(EventStatus.CLOSED);
    }

    @Test
    void fromReturnsOngoingWhenTodayIsWithinEventPeriod() {
        EventStatus status = EventStatus.from(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                TODAY);

        assertThat(status).isEqualTo(EventStatus.ONGOING);
    }
}
