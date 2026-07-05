package com.closing.closing.domain.business.entity;

import com.closing.closing.domain.user.entity.User;
import com.closing.closing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "business_registrations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BusinessRegistration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String businessNumber;

    private String businessOwnerName;

    private LocalDateTime businessOpenDate;

    private LocalDateTime businessCloseDate;

    private LocalDate closePlannedDate;

    @Builder
    public BusinessRegistration(User user, String businessNumber, String businessOwnerName,
                                LocalDateTime businessOpenDate, LocalDateTime businessCloseDate,
                                LocalDate closePlannedDate) {
        this.user = user;
        this.businessNumber = businessNumber;
        this.businessOwnerName = businessOwnerName;
        this.businessOpenDate = businessOpenDate;
        this.businessCloseDate = businessCloseDate;
        this.closePlannedDate = closePlannedDate;
    }

}