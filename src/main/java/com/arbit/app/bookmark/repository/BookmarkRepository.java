package com.arbit.app.bookmark.repository;

import com.arbit.app.bookmark.entity.Bookmark;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserIdAndEventId(UUID userId, UUID eventId);

    void deleteAllByUserId(UUID userId);

    @Query("""
            select b
            from Bookmark b
            join fetch b.event e
            join fetch e.category
            where b.user.id = :userId
            order by b.createdAt desc
            """)
    List<Bookmark> findAllByUserIdWithEvent(@Param("userId") UUID userId);

    @Query("""
            select b.event.id
            from Bookmark b
            where b.user.id = :userId
            """)
    List<UUID> findEventIdsByUserId(@Param("userId") UUID userId);
}
