package com.arbit.app.review.entity;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_user_event", columnNames = {"user_id", "event_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 200)
    private String content;

    @Column(length = 500)
    private String verificationImageUrl;

    @Builder
    private Review(User user, Event event, int rating, String content, String verificationImageUrl) {
        this.user = user;
        this.event = event;
        this.rating = rating;
        this.content = content;
        this.verificationImageUrl = verificationImageUrl;
    }
}
