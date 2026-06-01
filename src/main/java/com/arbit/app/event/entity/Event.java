package com.arbit.app.event.entity;

import com.arbit.app.category.entity.Category;
import com.arbit.app.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseTimeEntity {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 1000)
    private String posterImageUrl;

    @Column(nullable = false, length = 100)
    private String venue;

    @Column(length = 255)
    private String venueAddress;

    @Column(nullable = false, length = 50)
    private String district;

    private Double latitude;

    private Double longitude;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean free;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(length = 255)
    private String price;

    @Column(length = 1000)
    private String bookingUrl;

    @Builder
    private Event(Category category, String title, String description, String posterImageUrl, String venue,
                  String venueAddress, String district, Double latitude, Double longitude,
                  LocalDate startDate, LocalDate endDate, boolean free, EventStatus status,
                  String price, String bookingUrl) {
        this.id = UUID.randomUUID();
        this.category = category;
        this.title = title;
        this.description = description;
        this.posterImageUrl = posterImageUrl;
        this.venue = venue;
        this.venueAddress = venueAddress;
        this.district = district;
        this.latitude = latitude;
        this.longitude = longitude;
        this.startDate = startDate;
        this.endDate = endDate;
        this.free = free;
        this.status = status;
        this.averageRating = BigDecimal.ZERO;
        this.price = price;
        this.bookingUrl = bookingUrl;
    }

    public void updateAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public boolean updateStatus(LocalDate today) {
        EventStatus newStatus = EventStatus.from(startDate, endDate, today);
        if (status == newStatus) {
            return false;
        }
        this.status = newStatus;
        return true;
    }
}
