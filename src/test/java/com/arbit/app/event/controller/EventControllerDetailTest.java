package com.arbit.app.event.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.event.dto.EventDetailResponse;
import com.arbit.app.event.entity.EventStatus;
import com.arbit.app.event.service.EventSearchService;
import com.arbit.app.event.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class EventControllerDetailTest {

    @Mock
    private EventService eventService;

    @Mock
    private EventSearchService eventSearchService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(new EventController(eventService, eventSearchService))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getEventDetailReturnsSwaggerDataFields() throws Exception {
        UUID eventId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        EventDetailResponse response = new EventDetailResponse(
                eventId,
                "Echoes of Silence",
                "Media Art",
                "https://cdn.arbit.app/events/light-museum/poster.jpg",
                "https://example.com/events/echoes-of-silence",
                "Jongno-gu",
                "Metropolitan Museum",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 30),
                "20,000",
                null,
                false,
                List.of("painting", "solo"),
                EventStatus.ONGOING,
                BigDecimal.valueOf(4.7),
                true
        );
        when(eventService.getEventDetail(eq(eventId), any(CustomUserDetails.class))).thenReturn(response);

        mockMvc.perform(get("/api/events/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.event_id").value(eventId.toString()))
                .andExpect(jsonPath("$.data.title").value("Echoes of Silence"))
                .andExpect(jsonPath("$.data.category").value("Media Art"))
                .andExpect(jsonPath("$.data.posterImageUrl").value("https://cdn.arbit.app/events/light-museum/poster.jpg"))
                .andExpect(jsonPath("$.data.url").value("https://example.com/events/echoes-of-silence"))
                .andExpect(jsonPath("$.data.district").value("Jongno-gu"))
                .andExpect(jsonPath("$.data.venue").value("Metropolitan Museum"))
                .andExpect(jsonPath("$.data.startDate").value("2026-05-01"))
                .andExpect(jsonPath("$.data.endDate").value("2026-06-30"))
                .andExpect(jsonPath("$.data.price").value("20,000"))
                .andExpect(jsonPath("$.data.time").doesNotExist())
                .andExpect(jsonPath("$.data.free").value(false))
                .andExpect(jsonPath("$.data.keyword[0]").value("painting"))
                .andExpect(jsonPath("$.data.keyword[1]").value("solo"))
                .andExpect(jsonPath("$.data.status").value("ONGOING"))
                .andExpect(jsonPath("$.data.rating").value(4.7))
                .andExpect(jsonPath("$.data.bookmarked").value(true));

        verify(eventService).getEventDetail(eq(eventId), any(CustomUserDetails.class));
    }
}
