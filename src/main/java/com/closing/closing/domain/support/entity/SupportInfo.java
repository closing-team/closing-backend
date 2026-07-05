package com.closing.closing.domain.support.entity;

import com.closing.closing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "support_info")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupportInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "support_id")
    private Long id;

    @Column(nullable = false)
    private String organizationName;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDate applyStartDate;

    private LocalDate applyEndDate;

    @Column(columnDefinition = "TEXT")
    private String externalUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportStatus status = SupportStatus.ONGOING;

    @Column(nullable = false)
    private int viewCount = 0;

    @Builder
    public SupportInfo(String organizationName, String title, String content,
                       LocalDate applyStartDate, LocalDate applyEndDate,
                       String externalUrl, SupportStatus status) {
        this.organizationName = organizationName;
        this.title = title;
        this.content = content;
        this.applyStartDate = applyStartDate;
        this.applyEndDate = applyEndDate;
        this.externalUrl = externalUrl;
        this.status = status;
    }

}
