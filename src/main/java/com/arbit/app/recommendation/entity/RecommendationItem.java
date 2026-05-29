package com.arbit.app.recommendation.entity;

import com.arbit.app.common.entity.BaseTimeEntity;
import com.arbit.app.event.entity.Event;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "recommendation_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_recommendation_item_run_event", columnNames = {"run_id", "event_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendationItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private RecommendationRun run;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private int rankNo;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal score;

    @Column(nullable = false, length = 300)
    private String reason;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String featureScoresJson;

    @Builder
    private RecommendationItem(RecommendationRun run, Event event, int rankNo, BigDecimal score,
                               String reason, String featureScoresJson) {
        this.run = run;
        this.event = event;
        this.rankNo = rankNo;
        this.score = score;
        this.reason = reason;
        this.featureScoresJson = featureScoresJson;
    }
}
