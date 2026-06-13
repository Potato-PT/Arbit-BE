package com.arbit.app.user.entity;

import com.arbit.app.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String nickname;

    @Column(length = 1000)
    private String profileImageUrl;

    @Column
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserGender gender;

    @Column(length = 255)
    private String residentialArea;

    @Column()
    private Double residentialLatitude;

    @Column()
    private Double residentialLongitude;

    // @Enumerated(EnumType.STRING)
    // @Column(nullable = false, length = 20)
    // private UserRole role;

    @Builder
    private User(String username, String password, String nickname, String profileImageUrl, Integer age, UserGender gender,
                 String residentialArea, Double residentialLatitude, Double residentialLongitude) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.age = age;
        this.gender = gender;
        this.residentialArea = residentialArea;
        this.residentialLatitude = residentialLatitude;
        this.residentialLongitude = residentialLongitude;
        // this.role = role == null ? UserRole.OTHER : role;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void initializeResidentialLocationIfMissing(String residentialArea, double latitude, double longitude) {
        if (this.residentialArea != null && !this.residentialArea.isBlank()
                && this.residentialLatitude != null && this.residentialLongitude != null) {
            return;
        }
        this.residentialArea = residentialArea;
        this.residentialLatitude = latitude;
        this.residentialLongitude = longitude;
    }
}
