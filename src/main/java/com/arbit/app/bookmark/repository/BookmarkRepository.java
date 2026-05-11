package com.arbit.app.bookmark.repository;

import com.arbit.app.bookmark.entity.Bookmark;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserIdAndEventId(UUID userId, UUID eventId);
}
