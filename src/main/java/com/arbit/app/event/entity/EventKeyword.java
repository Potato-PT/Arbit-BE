package com.arbit.app.event.entity;

import com.arbit.app.common.entity.BaseTimeEntity;
import com.arbit.app.keyword.entity.PreferenceKeyword;
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
@Table(name = "event_keywords", uniqueConstraints = {
        @UniqueConstraint(name = "uk_event_keyword", columnNames = {"event_id", "preference_keyword_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventKeyword extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "preference_keyword_id", nullable = false)
    private PreferenceKeyword preferenceKeyword;
}
