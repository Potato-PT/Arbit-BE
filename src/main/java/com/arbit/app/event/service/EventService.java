package com.arbit.app.event.service;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.bookmark.repository.BookmarkRepository;
import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import com.arbit.app.event.dto.EventDetailResponse;
import com.arbit.app.event.dto.EventResponse;
import com.arbit.app.event.entity.Event;
import com.arbit.app.event.entity.EventStatus;
import com.arbit.app.event.repository.EventKeywordRepository;
import com.arbit.app.event.repository.EventRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final EventKeywordRepository eventKeywordRepository;
    private final BookmarkRepository bookmarkRepository;
    private final EventActionService eventActionService;

    public EventService(EventRepository eventRepository, EventKeywordRepository eventKeywordRepository,
                        BookmarkRepository bookmarkRepository, EventActionService eventActionService) {
        this.eventRepository = eventRepository;
        this.eventKeywordRepository = eventKeywordRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.eventActionService = eventActionService;
    }

    @Transactional
    public EventDetailResponse getEventDetail(UUID eventId, CustomUserDetails userDetails) {
        Event event = eventRepository.findWithCategoryById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
        List<String> keywords = eventKeywordRepository.findKeywordValuesByEventId(eventId);
        boolean bookmarked = userDetails != null && bookmarkRepository.existsByUserIdAndEventId(userDetails.id(), eventId);
        saveDetailViewLog(userDetails, event);
        return EventDetailResponse.from(event, keywords, bookmarked);
    }

    public List<EventResponse> getEvents(String category, List<String> districts, LocalDate startDate,
                                         LocalDate endDate, String sort, EventStatus status) {
        List<String> normalizedDistricts = normalize(districts);
        return eventRepository.findByStatusOrderByEndDateAsc(
                        status == EventStatus.ONGOING,
                        status == EventStatus.UPCOMING,
                        status == EventStatus.CLOSED,
                        EventStatus.today(),
                        normalize(category),
                        normalizedDistricts != null,
                        normalizedDistricts == null ? List.of("__NO_DISTRICT__") : normalizedDistricts,
                        startDate,
                        endDate,
                        normalizeSort(sort)).stream()
                .map(EventResponse::from)
                .toList();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private List<String> normalize(List<String> values) {
        if (values == null) {
            return null;
        }
        if (values.stream().anyMatch(value -> value == null || value.isBlank())) {
            return null;
        }
        List<String> normalized = values.stream()
                .toList();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeSort(String sort) {
        if ("latest".equals(sort) || "rating".equals(sort) || "match".equals(sort)) {
            return sort;
        }
        return "deadline";
    }

    private void saveDetailViewLog(CustomUserDetails userDetails, Event event) {
        if (userDetails == null) {
            return;
        }
        eventActionService.recordDetailView(userDetails.id(), event);
    }
}
