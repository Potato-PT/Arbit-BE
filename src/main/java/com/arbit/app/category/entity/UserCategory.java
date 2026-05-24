package com.arbit.app.category.entity;

import com.arbit.app.common.entity.BaseTimeEntity;
import com.arbit.app.user.entity.User;
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
@Table(name = "user_categories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_category", columnNames = {"user_id", "category_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Builder
    private UserCategory(User user, Category category) {
        this.user = user;
        this.category = category;
    }
}
