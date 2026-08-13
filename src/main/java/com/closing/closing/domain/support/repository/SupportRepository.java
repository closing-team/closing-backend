package com.closing.closing.domain.support.repository;

import com.closing.closing.domain.support.entity.SupportInfo;
import com.closing.closing.domain.support.entity.SupportStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SupportRepository extends JpaRepository<SupportInfo, Long> {

    Optional<SupportInfo> findByExternalId(String externalId);

    /**
     * externalId 도입 전에는 공고 URL을 external_url에 저장했으므로
     * 아직 전환되지 않은 레거시 데이터만 폴백 조회합니다.
     */
    @Query("""
            SELECT s FROM SupportInfo s
            WHERE s.externalId IS NULL
              AND s.applicationUrl = :announcementUrl
            """)
    Optional<SupportInfo> findLegacyByAnnouncementUrl(
            @Param("announcementUrl") String announcementUrl);

    List<SupportInfo> findAllByExternalIdIsNotNullOrApplicationUrlStartingWith(
            String applicationUrlPrefix);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE SupportInfo s
            SET s.viewCount = s.viewCount + 1,
                s.updatedAt = CURRENT_TIMESTAMP
            WHERE s.id = :supportId
            """)
    int increaseViewCount(@Param("supportId") Long supportId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE SupportInfo s
            SET s.organizationName = :organizationName,
                s.title = :title,
                s.content = :content,
                s.applyStartDate = :applyStartDate,
                s.applyEndDate = :applyEndDate,
                s.applicationPeriod = :applicationPeriod,
                s.externalId = :externalId,
                s.applicationUrl = :applicationUrl,
                s.status = :status,
                s.viewCount = CASE
                    WHEN s.viewCount < :externalViewCount THEN :externalViewCount
                    ELSE s.viewCount
                END,
                s.updatedAt = CURRENT_TIMESTAMP
            WHERE s.id = :supportId
            """)
    int updateFromExternal(
            @Param("supportId") Long supportId,
            @Param("organizationName") String organizationName,
            @Param("title") String title,
            @Param("content") String content,
            @Param("applyStartDate") LocalDate applyStartDate,
            @Param("applyEndDate") LocalDate applyEndDate,
            @Param("applicationPeriod") String applicationPeriod,
            @Param("externalId") String externalId,
            @Param("applicationUrl") String applicationUrl,
            @Param("status") SupportStatus status,
            @Param("externalViewCount") int externalViewCount);

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
            ORDER BY s.createdAt DESC, s.id DESC
            """)
    List<SupportInfo> findAllByLatest(Pageable pageable);

    @Query("""
            SELECT s FROM SupportInfo s
            WHERE (s.createdAt < :cursorCreatedAt
                OR (s.createdAt = :cursorCreatedAt AND s.id < :cursorId))
            ORDER BY s.createdAt DESC, s.id DESC
            """)
    List<SupportInfo> findAllByLatestAfterCursor(
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
