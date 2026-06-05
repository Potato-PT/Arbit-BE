package com.arbit.app.bookmark.service;

import com.arbit.app.bookmark.entity.Bookmark;
import com.arbit.app.bookmark.repository.BookmarkRepository;
import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import com.arbit.app.event.entity.Event;
import com.arbit.app.event.repository.EventRepository;
import com.arbit.app.user.entity.User;
import com.arbit.app.user.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository,
                           EventRepository eventRepository,
                           UserRepository userRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void addBookmark(UUID userId, UUID eventId) {
        if (bookmarkRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        bookmarkRepository.save(Bookmark.builder()
                .user(user)
                .event(event)
                .build());
    }

    @Transactional
    public void removeBookmark(UUID userId, UUID eventId) {
        Bookmark bookmark = bookmarkRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        bookmarkRepository.delete(bookmark);
    }
}
