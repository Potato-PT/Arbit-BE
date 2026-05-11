package com.arbit.app.event.entity;

import com.arbit.app.common.entity.BaseTimeEntity;
import com.arbit.app.keyword.entity.ClassificationKeyword;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "event_classifications", uniqueConstraints = {
        @UniqueConstraint(name = "uk_event_classification", columnNames = {"event_id", "classification_keyword_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventClassification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "classification_keyword_id", nullable = false)
    private ClassificationKeyword classificationKeyword;
}
