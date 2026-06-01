package com.arbit.app.event.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.arbit.app.event.entity.Event;
import com.arbit.app.event.entity.EventStatus;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:mysql://127.0.0.1:3306/arbit_local?serverTimezone=Asia/Seoul&characterEncoding=UTF-8",
        "spring.datasource.username=root",
        "spring.datasource.password=root",
        "jwt.secret=GCPJwtSecretKey123GCPJwtSecretKey123GCPJwtSecretKey123!",
        "kakao.local.rest-api-key=local-test-key"
})
class EventRepositorySearchLocalDbTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    void searchEventsFindsTitleSuggestionFromLocalDb() {
        List<Event> events = eventRepository.searchEvents(
                "어슬렁",
                "TITLE",
                null,
                false,
                List.of("__NO_DISTRICT__"),
                false,
                false,
                false,
                false,
                null,
                LocalDate.now()
        );

        assertThat(events)
                .extracting(Event::getTitle)
                .anyMatch(title -> title.contains("어슬렁"));
    }
}
