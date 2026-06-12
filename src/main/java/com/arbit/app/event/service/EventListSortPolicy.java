package com.arbit.app.event.service;

import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import java.util.Set;

public final class EventListSortPolicy {

    public static final String DEFAULT_SORT = "deadline";
    public static final String MATCH_SORT = "match";
    private static final Set<String> SUPPORTED_SORTS = Set.of(MATCH_SORT, DEFAULT_SORT, "latest", "rating");
    private static final String INVALID_SORT_MESSAGE =
            "sort must be provided once with one of: match, deadline, latest, rating.";

    private EventListSortPolicy() {
    }

    public static String normalize(String sort) {
        if (sort == null) {
            return DEFAULT_SORT;
        }
        if (sort.isBlank() || !SUPPORTED_SORTS.contains(sort)) {
            throw invalidSort();
        }
        return sort;
    }

    public static String normalize(String[] sortValues) {
        if (sortValues == null) {
            return DEFAULT_SORT;
        }
        if (sortValues.length != 1) {
            throw invalidSort();
        }
        return normalize(sortValues[0]);
    }

    public static boolean requiresAuthentication(String[] sortValues) {
        return MATCH_SORT.equals(normalize(sortValues));
    }

    private static BusinessException invalidSort() {
        return new BusinessException(ErrorCode.INVALID_REQUEST, INVALID_SORT_MESSAGE);
    }
}
