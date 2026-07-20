package com.closing.closing.domain.support.repository;

import com.closing.closing.domain.support.entity.SupportInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SupportRepository extends JpaRepository<SupportInfo, Long> {

    Optional<SupportInfo> findByExternalUrl(String externalUrl);

    List<SupportInfo> findAllByExternalUrlStartingWith(String externalUrlPrefix);

    @Query("""
            SELECT s FROM SupportInfo s
            WHERE (:cursorViewCount IS NULL
                OR s.viewCount < :cursorViewCount
                OR (s.viewCount = :cursorViewCount AND s.id < :cursorId))
            ORDER BY s.viewCount DESC, s.id DESC
            """)
    List<SupportInfo> findAllByPopular(
            @Param("cursorViewCount") Integer cursorViewCount,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    @Query("""
            SELECT s FROM SupportInfo s
            WHERE (:cursorCreatedAt IS NULL
                OR s.createdAt < :cursorCreatedAt
                OR (s.createdAt = :cursorCreatedAt AND s.id < :cursorId))
            ORDER BY s.createdAt DESC, s.id DESC
            """)
    List<SupportInfo> findAllByLatest(
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    @Query("""
            SELECT s FROM SupportInfo s
            WHERE (:cursorEndDate IS NULL
                OR COALESCE(s.applyEndDate, :lastEndDate) > :cursorEndDate
                OR (COALESCE(s.applyEndDate, :lastEndDate) = :cursorEndDate
                    AND s.id > :cursorId))
            ORDER BY COALESCE(s.applyEndDate, :lastEndDate) ASC, s.id ASC
            """)
    List<SupportInfo> findAllByDeadline(
            @Param("cursorEndDate") LocalDate cursorEndDate,
            @Param("cursorId") Long cursorId,
            @Param("lastEndDate") LocalDate lastEndDate,
            Pageable pageable);
}
