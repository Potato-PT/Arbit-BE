package com.arbit.app.review.repository;

import com.arbit.app.review.entity.Review;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndEventId(UUID userId, UUID eventId);

    Optional<Review> findByUserIdAndEventId(UUID userId, UUID eventId);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.event.id = :eventId")
    double averageRatingByEventId(@Param("eventId") UUID eventId);
}
