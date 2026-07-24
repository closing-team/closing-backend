package com.closing.closing.domain.user.entity;

import com.closing.closing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String kakaoId;

    @Column(nullable = false, length = 50)
    private String nickname;

    private String name;

    private String phone;

    private String email;

    @Column(columnDefinition = "TEXT")
    private String profileImageUrl;

    private LocalDateTime deletedAt;

    @Builder
    public User(String kakaoId, String nickname, String name, String phone,
                String email, String profileImageUrl) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }

    public void completeSignup(String name, String nickname, String phone,
                               String email, String profileImageUrl) {
        this.name = name;
        this.nickname = nickname;
        this.phone = phone;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }

    public void updateInfo(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public void withdraw() {
        this.deletedAt = LocalDateTime.now();
    }
}