package com.closing.closing.domain.terms.entity;

import com.closing.closing.global.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "terms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Term extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "term_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TermType type;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private boolean isRequired;

    @Builder
    public Term(TermType type, String version, String content,
                LocalDate effectiveDate, boolean isRequired) {
        this.type = type;
        this.version = version;
        this.content = content;
        this.effectiveDate = effectiveDate;
        this.isRequired = isRequired;
    }
}