package com.arbit.app.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arbit.app.category.entity.Category;
import com.arbit.app.event.dto.EventSearchSuggestionsResponse;
import com.arbit.app.event.dto.EventSearchTarget;
import com.arbit.app.event.dto.EventSuggestionItem;
import com.arbit.app.event.entity.Event;
import com.arbit.app.event.entity.EventStatus;
import com.arbit.app.event.repository.EventRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EventSearchServiceTest {

    @Mock
    private EventRepository eventRepository;

    @ParameterizedTest(name = "{index}. {0}")
    @MethodSource("titleSuggestionCases")
    void getSuggestionsReturnsTitleMatchedEvent(
            String description,
            String keyword,
            UUID eventId,
            String title,
            String category,
            String venue,
            String district,
            LocalDate startDate,
            LocalDate endDate,
            String price,
            boolean free,
            EventStatus status
    ) {
        EventSearchService eventSearchService = new EventSearchService(eventRepository);
        Event event = event(eventId, title, category, venue, district, startDate, endDate, price, free, status);
        when(eventRepository.searchEvents(
                eq(keyword),
                eq(EventSearchTarget.TITLE.name()),
                isNull(),
                eq(false),
                eq(List.of("__NO_DISTRICT__")),
                eq(false),
                eq(false),
                eq(false),
                eq(false),
                isNull(),
                any(LocalDate.class)
        )).thenReturn(List.of(event));

        EventSearchSuggestionsResponse response = eventSearchService.getSuggestions(
                keyword,
                EventSearchTarget.TITLE,
                10
        );

        assertThat(response.keyword()).isEqualTo(keyword);
        assertThat(response.target()).isEqualTo(EventSearchTarget.TITLE);
        assertThat(response.suggestions())
                .extracting(EventSuggestionItem::eventId)
                .containsExactly(eventId);

        EventSuggestionItem suggestion = response.suggestions().get(0);
        assertThat(suggestion.title()).isEqualTo(title);
        assertThat(suggestion.category()).isEqualTo(category);
        assertThat(suggestion.venue()).isEqualTo(venue);
        assertThat(suggestion.district()).isEqualTo(district);
        assertThat(suggestion.price()).isEqualTo(price);
        assertThat(suggestion.free()).isEqualTo(free);
        assertThat(suggestion.matchedField()).isEqualTo(EventSearchTarget.TITLE);
        assertThat(suggestion.highlightText()).isEqualTo(title);

        verify(eventRepository).searchEvents(
                eq(keyword),
                eq(EventSearchTarget.TITLE.name()),
                isNull(),
                eq(false),
                eq(List.of("__NO_DISTRICT__")),
                eq(false),
                eq(false),
                eq(false),
                eq(false),
                isNull(),
                any(LocalDate.class)
        );
    }

    static Stream<Arguments> titleSuggestionCases() {
        return Stream.of(
                titleSuggestionCase(
                        "사용자가 어슬렁을 입력하면 영등포문화재단 콘서트가 자동완성에 노출된다.",
                        "어슬렁",
                        "b85db261-a54d-4146-ac99-e070f88e9cf4",
                        "[영등포문화재단] 어슬렁 어슬렁 콘서트 #여름 [죠지&김뜻돌]",
                        "콘서트",
                        "영등포아트홀",
                        "영등포구",
                        LocalDate.of(2026, 7, 24),
                        LocalDate.of(2026, 7, 24),
                        "전석 45,000원",
                        false,
                        EventStatus.UPCOMING
                ),
                titleSuggestionCase(
                        "사용자가 더블베이스를 입력하면 서리풀 작곡가 탐구 시리즈가 추천된다.",
                        "더블베이스",
                        "a5800865-4c5f-43cf-bb90-7551939c7888",
                        "[서초문화재단] 2026 서리풀 작곡가 탐구 시리즈 [더블베이스의 시작과 여정]",
                        "클래식",
                        "반포심산아트홀",
                        "서초구",
                        LocalDate.of(2026, 5, 30),
                        LocalDate.of(2026, 5, 30),
                        "R석 2만원 S석 1만5천원",
                        false,
                        EventStatus.CLOSED
                ),
                titleSuggestionCase(
                        "사용자가 별모빌을 입력하면 서울책보고 체험형 북토크가 자동완성에 나온다.",
                        "별모빌",
                        "9b7023c4-8f36-43c5-9c2a-4f8daa0d63e2",
                        "[서울책보고] 큐레이션 토크 with 키즈엠 [나만의 별모빌 만들기 - 체험형 북토크]",
                        "교육/체험",
                        "서울책보고 라운지",
                        "송파구",
                        LocalDate.of(2026, 5, 9),
                        LocalDate.of(2026, 5, 9),
                        null,
                        true,
                        EventStatus.CLOSED
                ),
                titleSuggestionCase(
                        "사용자가 주얼리를 입력하면 AI 융합 주얼리 교육 이벤트가 검색 후보로 표시된다.",
                        "주얼리",
                        "05f1550e-8aa4-4d5a-93ab-f0fa79a8c949",
                        "[서울시지원/ 전액무료] AI 융합 주얼리 트렌드 MD 및 비주얼 스타일링 실무",
                        "교육/체험",
                        "종로여성인력개발센터",
                        "종로구",
                        LocalDate.of(2026, 5, 28),
                        LocalDate.of(2026, 9, 2),
                        null,
                        true,
                        EventStatus.ONGOING
                ),
                titleSuggestionCase(
                        "사용자가 바냐를 입력하면 체홉 낭독극 바냐 아저씨가 추천된다.",
                        "바냐",
                        "04ba876b-fc95-4b84-94ef-589cb670459c",
                        "[마포문화재단] 체홉 4대 장막 낭독극 [공놀이클럽의 사계절 체홉: 바냐 아저씨]",
                        "연극",
                        "마포아트센터 플레이맥",
                        "마포구",
                        LocalDate.of(2026, 6, 20),
                        LocalDate.of(2026, 6, 20),
                        "전석 1만원",
                        false,
                        EventStatus.UPCOMING
                ),
                titleSuggestionCase(
                        "사용자가 미미한을 입력하면 연극 미미의 미미한 연애가 자동완성된다.",
                        "미미한",
                        "b9e175d6-0d73-44f9-946d-43dcb73c0cd3",
                        "[마포문화재단] 연극 [미미의 미미한 연애]",
                        "연극",
                        "마포아트센터 플레이맥",
                        "마포구",
                        LocalDate.of(2026, 6, 3),
                        LocalDate.of(2026, 6, 7),
                        "전석 50,000원",
                        false,
                        EventStatus.UPCOMING
                ),
                titleSuggestionCase(
                        "사용자가 장미축제를 입력하면 중랑 서울장미축제가 추천 리스트에 나온다.",
                        "장미축제",
                        "8c2fe70e-eff2-4c1e-bb4b-cf14b3179d07",
                        "[중랑문화재단] 제 18회 중랑 서울장미축제",
                        "축제-자연/경관",
                        "중랑장미공원",
                        "중랑구",
                        LocalDate.of(2026, 5, 15),
                        LocalDate.of(2026, 5, 23),
                        "무료",
                        true,
                        EventStatus.CLOSED
                ),
                titleSuggestionCase(
                        "사용자가 사진으로를 입력하면 사진으로 만나는 장면들 이벤트가 미리보기 후보로 표시된다.",
                        "사진으로",
                        "ffbc3d2e-1455-4295-a4f6-7cf53ad04cc7",
                        "[서울문화예술교육센터 양천] 예술작업실 [사진으로 만나는 장면들]",
                        "교육/체험",
                        "서울문화예술교육센터 양천 다목적홀, 야외수조",
                        "양천구",
                        LocalDate.of(2026, 5, 9),
                        LocalDate.of(2026, 5, 30),
                        null,
                        true,
                        EventStatus.CLOSED
                ),
                titleSuggestionCase(
                        "사용자가 별 헤는 밤을 입력하면 운현궁 안내 이벤트가 자동완성된다.",
                        "별 헤는 밤",
                        "c89f8bc8-e48e-43cb-aa8b-84a6e0667918",
                        "[운현궁] 2026 별 헤는 밤 운현궁 안내(5월)",
                        "교육/체험",
                        "서울 운현궁",
                        "종로구",
                        LocalDate.of(2026, 5, 29),
                        LocalDate.of(2026, 5, 29),
                        "2인 1팀 30,000원",
                        false,
                        EventStatus.CLOSED
                ),
                titleSuggestionCase(
                        "사용자가 사서의 가방을 입력하면 금천구립가산도서관 다문화페스타가 추천된다.",
                        "사서의 가방",
                        "6802e35a-1933-4697-af77-d4164bd50daf",
                        "[금천구립가산도서관] 다문화페스타 [사서의 가방 속  세 나라 이야기]",
                        "교육/체험",
                        "금천구립가산도서관 6층 강의실",
                        "금천구",
                        LocalDate.of(2026, 5, 9),
                        LocalDate.of(2026, 5, 21),
                        "무료",
                        true,
                        EventStatus.CLOSED
                )
        );
    }

    private static Arguments titleSuggestionCase(
            String description,
            String keyword,
            String eventId,
            String title,
            String category,
            String venue,
            String district,
            LocalDate startDate,
            LocalDate endDate,
            String price,
            boolean free,
            EventStatus status
    ) {
        return Arguments.of(
                description,
                keyword,
                UUID.fromString(eventId),
                title,
                category,
                venue,
                district,
                startDate,
                endDate,
                price,
                free,
                status
        );
    }

    private Event event(
            UUID eventId,
            String title,
            String categoryName,
            String venue,
            String district,
            LocalDate startDate,
            LocalDate endDate,
            String price,
            boolean free,
            EventStatus status
    ) {
        Event event = Event.builder()
                .category(Category.builder().name(categoryName).build())
                .title(title)
                .description(null)
                .posterImageUrl(null)
                .venue(venue)
                .venueAddress(null)
                .district(district)
                .latitude(null)
                .longitude(null)
                .startDate(startDate)
                .endDate(endDate)
                .free(free)
                .status(status)
                .price(price)
                .bookingUrl(null)
                .build();
        ReflectionTestUtils.setField(event, "id", eventId);
        return event;
    }
}
