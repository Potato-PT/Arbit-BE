package com.arbit.app.event.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arbit.app.event.dto.EventSearchResultsResponse;
import com.arbit.app.event.dto.EventSearchSort;
import com.arbit.app.event.dto.EventSearchSuggestionsResponse;
import com.arbit.app.event.dto.EventSearchTarget;
import com.arbit.app.event.entity.EventStatus;
import com.arbit.app.event.service.EventSearchService;
import com.arbit.app.event.service.EventService;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@ExtendWith(MockitoExtension.class)
class EventControllerSearchTest {

    @Mock
    private EventService eventService;

    @Mock
    private EventSearchService eventSearchService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new EventController(eventService, eventSearchService)).build();
        lenient().when(eventService.getEvents(any(), any(), any(), any(), anyString(), any(EventStatus.class)))
                .thenReturn(List.of());
    }

    @Test
    void suggestEventsUsesSearchServiceAndWrapsApiResponse() throws Exception {
        when(eventSearchService.getSuggestions("악뮤", EventSearchTarget.ALL, 10))
                .thenReturn(new EventSearchSuggestionsResponse("악뮤", EventSearchTarget.ALL, List.of()));

        mockMvc.perform(get("/api/events/search/suggestions")
                        .param("keyword", "악뮤")
                        .param("target", "ALL")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.keyword").value("악뮤"))
                .andExpect(jsonPath("$.data.target").value("ALL"))
                .andExpect(jsonPath("$.data.suggestions").isArray());

        verify(eventSearchService).getSuggestions("악뮤", EventSearchTarget.ALL, 10);
    }

    @Test
    void searchEventsUsesSearchServiceAndWrapsPagedResult() throws Exception {
        when(eventSearchService.search(
                "악뮤",
                EventSearchTarget.ALL,
                "콘서트",
                List.of("송파구"),
                EventStatus.ONGOING,
                false,
                EventSearchSort.deadline,
                null,
                null,
                0,
                20
        )).thenReturn(new EventSearchResultsResponse("악뮤", EventSearchTarget.ALL, 0, 20, 0, 0, List.of()));

        mockMvc.perform(get("/api/events/search")
                        .param("keyword", "악뮤")
                        .param("target", "ALL")
                        .param("category", "콘서트")
                        .param("district", "송파구")
                        .param("status", "ONGOING")
                        .param("free", "false")
                        .param("sort", "deadline")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.keyword").value("악뮤"))
                .andExpect(jsonPath("$.data.target").value("ALL"))
                .andExpect(jsonPath("$.data.items").isArray());

        verify(eventSearchService).search(
                "악뮤",
                EventSearchTarget.ALL,
                "콘서트",
                List.of("송파구"),
                EventStatus.ONGOING,
                false,
                EventSearchSort.deadline,
                null,
                null,
                0,
                20
        );
    }

    @ParameterizedTest(name = "{index}. {0}")
    @MethodSource("eventSearchCases")
    void getEventsAcceptsRegisteredSearchCases(
            String description,
            String category,
            List<String> districts,
            LocalDate startDate,
            LocalDate endDate
    ) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("sort", "deadline");
        if (category != null) {
            params.add("category", category);
        }
        if (districts != null) {
            districts.forEach(district -> params.add("district", district));
        }
        if (startDate != null) {
            params.add("startDate", startDate.toString());
        }
        if (endDate != null) {
            params.add("endDate", endDate.toString());
        }

        mockMvc.perform(get("/api/events").params(params))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(eventService).getEvents(
                eq(category),
                eq(districts),
                eq(startDate),
                eq(endDate),
                eq("deadline"),
                eq(EventStatus.ONGOING));
    }

    static Stream<Arguments> eventSearchCases() {
        return Stream.of(
                eventSearchCase("사용자는 분류와 자치구를 지정하지 않고 전체 서울 문화행사를 마감일이 빠른 순서로 검색했다.", null, null, null, null),
                eventSearchCase("사용자는 모든 자치구에서 클래식 행사만 검색했다.", "클래식", null, null, null),
                eventSearchCase("사용자는 모든 자치구에서 콘서트 행사만 검색했다.", "콘서트", null, null, null),
                eventSearchCase("사용자는 모든 자치구에서 전시/미술 행사만 검색했다.", "전시/미술", null, null, null),
                eventSearchCase("사용자는 마포구에서 열리는 모든 분류의 행사를 검색했다.", null, List.of("마포구"), null, null),
                eventSearchCase("사용자는 영등포구에서 열리는 모든 분류의 행사를 검색했다.", null, List.of("영등포구"), null, null),
                eventSearchCase("사용자는 마포구와 영등포구에서 열리는 행사를 함께 검색했다.", null, List.of("마포구", "영등포구"), null, null),
                eventSearchCase("사용자는 강남구와 종로구의 문화행사를 함께 검색했다.", null, List.of("강남구", "종로구"), null, null),
                eventSearchCase("사용자는 2026년 8월 1일 이후 시작하는 행사를 검색했다.", null, null, LocalDate.of(2026, 8, 1), null),
                eventSearchCase("사용자는 2026년 8월 31일까지 종료되는 행사를 검색했다.", null, null, null, LocalDate.of(2026, 8, 31)),
                eventSearchCase("사용자는 2026년 7월 안에 시작하고 종료되는 행사를 검색했다.", null, null, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31)),
                eventSearchCase("사용자는 2026년 6월부터 8월 말까지의 여름 시즌 행사를 검색했다.", null, null, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31)),
                eventSearchCase("사용자는 마포구에서 열리는 클래식 행사만 검색했다.", "클래식", List.of("마포구"), null, null),
                eventSearchCase("사용자는 영등포구에서 열리는 클래식 행사만 검색했다.", "클래식", List.of("영등포구"), null, null),
                eventSearchCase("사용자는 2026년 8월 중 마포구에서 열리는 콘서트 행사를 검색했다.", "콘서트", List.of("마포구"), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)),
                eventSearchCase("사용자는 2026년 8월 중 영등포구에서 열리는 무용 행사를 검색했다.", "무용", List.of("영등포구"), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)),
                eventSearchCase("사용자는 2026년 8월 중 강남구에서 열리는 전시/미술 행사를 검색했다.", "전시/미술", List.of("강남구"), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)),
                eventSearchCase("사용자는 2026년 6월 중 종로구에서 열리는 문화예술 축제를 검색했다.", "축제-문화/예술", List.of("종로구"), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30)),
                eventSearchCase("사용자는 2026년 7월 중 강서구에서 열리는 교육/체험 행사를 검색했다.", "교육/체험", List.of("강서구"), LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31)),
                eventSearchCase("사용자는 2026년 6월 중 서초구에서 열리는 국악 행사를 검색했다.", "국악", List.of("서초구"), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30)),
                eventSearchCase("사용자는 2026년 6월부터 7월까지 중구에서 열리는 연극 행사를 검색했다.", "연극", List.of("중구"), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 31)),
                eventSearchCase("사용자는 2026년 7월 중 송파구에서 열리는 뮤지컬/오페라 행사를 검색했다.", "뮤지컬/오페라", List.of("송파구"), LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31)),
                eventSearchCase("사용자는 2026년 9월 1일 이후 용산구에서 열리는 독주/독창회 행사를 검색했다.", "독주/독창회", List.of("용산구"), LocalDate.of(2026, 9, 1), null),
                eventSearchCase("사용자는 2026년 8월 말까지 종료되는 성동구의 기타 분류 행사를 검색했다.", "기타", List.of("성동구"), null, LocalDate.of(2026, 8, 31)),
                eventSearchCase("사용자는 2026년 9월 이후 마포구와 영등포구에서 열리는 클래식 행사를 검색했다.", "클래식", List.of("마포구", "영등포구"), LocalDate.of(2026, 9, 1), null),
                eventSearchCase("사용자는 2026년 9월 말까지 종료되는 마포구 행사를 분류 제한 없이 검색했다.", null, List.of("마포구"), null, LocalDate.of(2026, 9, 30)),
                eventSearchCase("사용자는 2026년 7월부터 8월까지 강서구, 마포구, 영등포구에서 열리는 행사를 검색했다.", null, List.of("강서구", "마포구", "영등포구"), LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 31)),
                eventSearchCase("사용자는 2026년 6월부터 7월까지 종로구, 중구, 용산구의 행사를 검색했다.", null, List.of("종로구", "중구", "용산구"), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 31)),
                eventSearchCase("사용자는 2026년 9월부터 연말까지 열리는 클래식 행사를 자치구 제한 없이 검색했다.", "클래식", null, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 31)),
                eventSearchCase("사용자는 2026년 8월 중 마포구에서 열리는 영화 행사를 검색했지만 조건에 맞는 결과가 없는 상황을 확인했다.", "영화", List.of("마포구"), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31))
        );
    }

    private static Arguments eventSearchCase(
            String description,
            String category,
            List<String> districts,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return Arguments.of(description, category, districts, startDate, endDate);
    }
}
