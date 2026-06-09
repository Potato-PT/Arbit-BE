package com.arbit.app.event.entity;

import com.arbit.app.common.entity.BaseTimeEntity;
import com.arbit.app.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "event_action_logs",
        indexes = {
                @Index(name = "idx_event_action_user_created", columnList = "user_id, created_at"),
                @Index(name = "idx_event_action_event_type_created", columnList = "event_id, action_type, created_at"),
                @Index(name = "idx_event_action_user_event_type", columnList = "user_id, event_id, action_type")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventActionLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private EventActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private EventActionSource source;

    @Builder
    private EventActionLog(User user, Event event, EventActionType actionType, EventActionSource source) {
        this.user = user;
        this.event = event;
        this.actionType = actionType;
        this.source = source;
    }
}
