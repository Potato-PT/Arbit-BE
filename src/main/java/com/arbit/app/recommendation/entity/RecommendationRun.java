package com.arbit.app.recommendation.entity;

import com.arbit.app.common.entity.BaseTimeEntity;
import com.arbit.app.user.entity.User;
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
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "recommendation_runs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendationRun extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String algorithm;

    @Column(nullable = false, length = 50)
    private String modelVersion;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String inputEventIdsJson;

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecommendationItem> items = new ArrayList<>();

    @Builder
    private RecommendationRun(User user, String algorithm, String modelVersion, String inputEventIdsJson) {
        this.user = user;
        this.algorithm = algorithm;
        this.modelVersion = modelVersion;
        this.inputEventIdsJson = inputEventIdsJson;
    }
}
