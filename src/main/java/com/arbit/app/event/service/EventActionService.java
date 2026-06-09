package com.arbit.app.event.service;

import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import com.arbit.app.event.entity.Event;
import com.arbit.app.event.entity.EventActionLog;
import com.arbit.app.event.entity.EventActionSource;
import com.arbit.app.event.entity.EventActionType;
import com.arbit.app.event.repository.EventActionLogRepository;
import com.arbit.app.event.repository.EventRepository;
import com.arbit.app.user.entity.User;
import com.arbit.app.user.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventActionService {

    private final EventActionLogRepository eventActionLogRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public EventActionService(EventActionLogRepository eventActionLogRepository,
                              UserRepository userRepository,
                              EventRepository eventRepository) {
        this.eventActionLogRepository = eventActionLogRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public void recordDetailView(UUID userId, Event event) {
        User user = getUser(userId);
        save(user, event, EventActionType.DETAIL_VIEW, null);
    }

    @Transactional
    public void recordHomepageClick(UUID userId, UUID eventId) {
        User user = getUser(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
        save(user, event, EventActionType.HOMEPAGE_CLICK, EventActionSource.EVENT_DETAIL);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    private void save(User user, Event event, EventActionType actionType, EventActionSource source) {
        eventActionLogRepository.save(EventActionLog.builder()
                .user(user)
                .event(event)
                .actionType(actionType)
                .source(source)
                .build());
    }
}
