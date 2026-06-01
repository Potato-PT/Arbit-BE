package com.arbit.app.preference.entity;

import com.arbit.app.common.entity.BaseTimeEntity;
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
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_preference_events", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_preference_event", columnNames = {"user_id", "event_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreferenceEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "event_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID eventId;

    @Builder
    private UserPreferenceEvent(User user, UUID eventId) {
        this.user = user;
        this.eventId = eventId;
    }
}
