package com.arbit.app.review.repository;

import com.arbit.app.review.entity.Review;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndEventId(UUID userId, UUID eventId);

    void deleteAllByUserId(UUID userId);

    Optional<Review> findByUserIdAndEventId(UUID userId, UUID eventId);

    List<Review> findAllByEventIdOrderByCreatedAtDesc(UUID eventId);

    @Query("""
            select r
            from Review r
            join fetch r.event e
            join fetch e.category
            where r.user.id = :userId
            order by r.updatedAt desc, r.createdAt desc
            """)
    List<Review> findAllByUserIdWithEvent(@Param("userId") UUID userId);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.event.id = :eventId")
    double averageRatingByEventId(@Param("eventId") UUID eventId);
}
