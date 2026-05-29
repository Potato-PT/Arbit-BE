package com.arbit.app.event.entity;

import com.arbit.app.common.entity.BaseTimeEntity;
import com.arbit.app.keyword.entity.Keyword;
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
@Table(name = "event_keyword_weights", uniqueConstraints = {
        @UniqueConstraint(name = "uk_event_keyword_weight", columnNames = {"event_id", "keyword_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventKeywordWeight extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal weight;

    @Column(nullable = false, length = 30)
    private String source;

    @Builder
    private EventKeywordWeight(Event event, Keyword keyword, BigDecimal weight, String source) {
        this.event = event;
        this.keyword = keyword;
        this.weight = weight;
        this.source = source;
    }
}
