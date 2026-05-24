package com.arbit.app.recommendation.repository;

import com.arbit.app.recommendation.entity.Recommendation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    void deleteAllByUserId(UUID userId);

    @EntityGraph(attributePaths = {"event", "event.category"})
    List<Recommendation> findByUserIdOrderByMatchScoreDesc(UUID userId);
}
