package com.arbit.app.recommendation.entity;

import com.arbit.app.common.entity.BaseTimeEntity;
import com.arbit.app.event.entity.Event;
import com.arbit.app.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "recommendations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_recommendation_user_event", columnNames = {"user_id", "event_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recommendation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "match_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal matchScore;

    @Column(nullable = false, length = 200)
    private String reason;

    @Builder
    private Recommendation(User user, Event event, BigDecimal matchScore, String reason) {
        this.user = user;
        this.event = event;
        this.matchScore = matchScore;
        this.reason = reason;
    }
}
