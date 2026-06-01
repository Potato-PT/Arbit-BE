package com.arbit.app.event.service;

import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import com.arbit.app.event.dto.EventSearchItem;
import com.arbit.app.event.dto.EventSearchResultsResponse;
import com.arbit.app.event.dto.EventSearchSort;
import com.arbit.app.event.dto.EventSearchSuggestionsResponse;
import com.arbit.app.event.dto.EventSearchTarget;
import com.arbit.app.event.dto.EventSuggestionItem;
import com.arbit.app.event.entity.Event;
import com.arbit.app.event.entity.EventStatus;
import com.arbit.app.event.repository.EventRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventSearchService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 20;
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    private final EventRepository eventRepository;

    public EventSearchService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public EventSearchSuggestionsResponse getSuggestions(String keyword, EventSearchTarget target, Integer limit) {
        String normalizedKeyword = requireKeyword(keyword);
        EventSearchTarget normalizedTarget = target == null ? EventSearchTarget.ALL : target;
        int normalizedLimit = normalizeLimit(limit);

        List<EventSuggestionItem> suggestions = eventRepository.searchEvents(
                        normalizedKeyword,
                        normalizedTarget.name(),
                        null,
                        false,
                        List.of("__NO_DISTRICT__"),
                        false,
                        false,
                        false,
                        false,
                        null,
                        EventStatus.today()).stream()
                .sorted(Comparator.comparing(Event::getEndDate).thenComparing(Event::getTitle))
                .limit(normalizedLimit)
                .map(event -> toSuggestion(event, normalizedKeyword, normalizedTarget))
                .toList();

        return new EventSearchSuggestionsResponse(normalizedKeyword, normalizedTarget, suggestions);
    }

    public EventSearchResultsResponse search(
            String keyword,
            EventSearchTarget target,
            String category,
            List<String> districts,
            EventStatus status,
            Boolean free,
            EventSearchSort sort,
            Double lat,
            Double lng,
            Integer page,
            Integer size
    ) {
        String normalizedKeyword = normalize(keyword);
        EventSearchTarget normalizedTarget = target == null ? EventSearchTarget.ALL : target;
        EventSearchSort normalizedSort = sort == null ? EventSearchSort.deadline : sort;
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        validateDistanceSort(normalizedSort, lat, lng);
        List<String> normalizedDistricts = normalize(districts);

        List<Event> events = eventRepository.searchEvents(
                normalizedKeyword,
                normalizedTarget.name(),
                normalize(category),
                normalizedDistricts != null,
                normalizedDistricts == null ? List.of("__NO_DISTRICT__") : normalizedDistricts,
                status != null,
                status == EventStatus.ONGOING,
                status == EventStatus.UPCOMING,
                status == EventStatus.CLOSED,
                free,
                EventStatus.today());

        List<EventSearchItem> items = events.stream()
                .sorted(comparator(normalizedSort, lat, lng))
                .skip((long) normalizedPage * normalizedSize)
                .limit(normalizedSize)
                .map(EventSearchItem::from)
                .toList();

        int totalPages = events.isEmpty() ? 0 : (int) Math.ceil((double) events.size() / normalizedSize);
        return new EventSearchResultsResponse(
                normalizedKeyword,
                normalizedTarget,
                normalizedPage,
                normalizedSize,
                events.size(),
                totalPages,
                items
        );
    }

    private EventSuggestionItem toSuggestion(Event event, String keyword, EventSearchTarget target) {
        EventSearchTarget matchedField = findMatchedField(event, keyword, target);
        return EventSuggestionItem.from(event, matchedField, highlightText(event, matchedField, keyword));
    }

    private EventSearchTarget findMatchedField(Event event, String keyword, EventSearchTarget target) {
        if (target != EventSearchTarget.ALL) {
            return target;
        }
        if (contains(event.getTitle(), keyword)) {
            return EventSearchTarget.TITLE;
        }
        if (contains(event.getCategory().getName(), keyword)) {
            return EventSearchTarget.CATEGORY;
        }
        if (contains(event.getVenue(), keyword)) {
            return EventSearchTarget.VENUE;
        }
        if (contains(event.getDistrict(), keyword)) {
            return EventSearchTarget.DISTRICT;
        }
        return EventSearchTarget.KEYWORD;
    }

    private String highlightText(Event event, EventSearchTarget matchedField, String keyword) {
        return switch (matchedField) {
            case TITLE -> event.getTitle();
            case CATEGORY -> event.getCategory().getName();
            case VENUE -> event.getVenue();
            case DISTRICT -> event.getDistrict();
            case KEYWORD -> eventRepository.findMatchedKeywordValues(event.getId(), keyword).stream()
                    .findFirst()
                    .orElse(event.getTitle());
            case ALL -> event.getTitle();
        };
    }

    private Comparator<Event> comparator(EventSearchSort sort, Double lat, Double lng) {
        return switch (sort) {
            case latest -> Comparator.comparing(Event::getStartDate).reversed().thenComparing(Event::getTitle);
            case rating -> Comparator.comparing(Event::getAverageRating).reversed()
                    .thenComparing(Event::getEndDate)
                    .thenComparing(Event::getTitle);
            case distance -> Comparator.<Event>comparingDouble(event -> distance(lat, lng, event))
                    .thenComparing(Event::getEndDate)
                    .thenComparing(Event::getTitle);
            case deadline -> Comparator.comparing(Event::getEndDate).thenComparing(Event::getTitle);
        };
    }

    private double distance(Double lat, Double lng, Event event) {
        if (event.getLatitude() == null || event.getLongitude() == null) {
            return Double.MAX_VALUE;
        }
        double latDistance = lat - event.getLatitude();
        double lngDistance = lng - event.getLongitude();
        return latDistance * latDistance + lngDistance * lngDistance;
    }

    private void validateDistanceSort(EventSearchSort sort, Double lat, Double lng) {
        if (sort == EventSearchSort.distance && (lat == null || lng == null)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "lat and lng are required when sort=distance.");
        }
    }

    private String requireKeyword(String keyword) {
        String normalizedKeyword = normalize(keyword);
        if (normalizedKeyword == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "keyword parameter is required.");
        }
        return normalizedKeyword;
    }

    private int normalizeLimit(Integer limit) {
        int normalizedLimit = limit == null ? DEFAULT_LIMIT : limit;
        if (normalizedLimit < 1 || normalizedLimit > MAX_LIMIT) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "limit must be between 1 and 20.");
        }
        return normalizedLimit;
    }

    private int normalizePage(Integer page) {
        int normalizedPage = page == null ? DEFAULT_PAGE : page;
        if (normalizedPage < 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "page must be greater than or equal to 0.");
        }
        return normalizedPage;
    }

    private int normalizeSize(Integer size) {
        int normalizedSize = size == null ? DEFAULT_SIZE : size;
        if (normalizedSize < 1 || normalizedSize > 100) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "size must be between 1 and 100.");
        }
        return normalizedSize;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private List<String> normalize(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<String> normalized = values.stream()
                .map(this::normalize)
                .filter(value -> value != null)
                .toList();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean contains(String source, String keyword) {
        return source != null && source.toLowerCase().contains(keyword.toLowerCase());
    }
}
