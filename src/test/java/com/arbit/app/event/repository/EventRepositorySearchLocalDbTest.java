package com.arbit.app.event.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.arbit.app.category.entity.Category;
import com.arbit.app.category.repository.CategoryRepository;
import com.arbit.app.event.entity.Event;
import com.arbit.app.event.entity.EventStatus;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:mysql://127.0.0.1:3306/arbit_local?serverTimezone=Asia/Seoul&characterEncoding=UTF-8",
        "spring.datasource.username=root",
        "spring.datasource.password=root",
        "jwt.secret=GCPJwtSecretKey123GCPJwtSecretKey123GCPJwtSecretKey123!",
        "kakao.local.rest-api-key=local-test-key"
})
@Transactional
class EventRepositorySearchLocalDbTest {

    private static final String SEARCH_KEYWORD = "ci-search-title";

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(Category.builder()
                .name("CI Search Category")
                .build());

        eventRepository.save(Event.builder()
                .category(category)
                .title("Arbit " + SEARCH_KEYWORD + " Event")
                .description("Repository search test event")
                .posterImageUrl("https://example.com/poster.jpg")
                .venue("CI Venue")
                .venueAddress("Seoul")
                .district("CI District")
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .free(true)
                .status(EventStatus.ONGOING)
                .price("free")
                .bookingUrl("https://example.com")
                .build());
    }

    @Test
    void searchEventsFindsTitleSuggestionFromLocalDb() {
        List<Event> events = eventRepository.searchEvents(
                SEARCH_KEYWORD,
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
                .anyMatch(title -> title.contains(SEARCH_KEYWORD));
    }
}
