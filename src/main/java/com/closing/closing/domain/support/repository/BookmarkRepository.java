package com.closing.closing.domain.support.repository;

import com.closing.closing.domain.support.entity.Bookmark;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUser_IdAndSupportInfo_Id(Long userId, Long supportId);

    Optional<Bookmark> findByUser_IdAndSupportInfo_Id(Long userId, Long supportId);

    @Query("""
            SELECT b FROM Bookmark b
            JOIN FETCH b.supportInfo s
            WHERE b.user.id = :userId
              AND (:cursorViewCount IS NULL
                OR s.viewCount < :cursorViewCount
                OR (s.viewCount = :cursorViewCount AND b.id < :cursorBookmarkId))
            ORDER BY s.viewCount DESC, b.id DESC
            """)
    List<Bookmark> findAllByPopular(
            @Param("userId") Long userId,
            @Param("cursorViewCount") Integer cursorViewCount,
            @Param("cursorBookmarkId") Long cursorBookmarkId,
            Pageable pageable);

    @Query("""
            SELECT b FROM Bookmark b
            JOIN FETCH b.supportInfo
            WHERE b.user.id = :userId
              AND (:cursorBookmarkId IS NULL OR b.id < :cursorBookmarkId)
            ORDER BY b.id DESC
            """)
    List<Bookmark> findAllByLatest(
            @Param("userId") Long userId,
            @Param("cursorBookmarkId") Long cursorBookmarkId,
            Pageable pageable);

    @Query("""
            SELECT b FROM Bookmark b
            JOIN FETCH b.supportInfo s
            WHERE b.user.id = :userId
              AND (:cursorEndDate IS NULL
                OR COALESCE(s.applyEndDate, :lastEndDate) > :cursorEndDate
                OR (COALESCE(s.applyEndDate, :lastEndDate) = :cursorEndDate
                    AND b.id > :cursorBookmarkId))
            ORDER BY COALESCE(s.applyEndDate, :lastEndDate) ASC, b.id ASC
            """)
    List<Bookmark> findAllByDeadline(
            @Param("userId") Long userId,
            @Param("cursorEndDate") LocalDate cursorEndDate,
            @Param("cursorBookmarkId") Long cursorBookmarkId,
            @Param("lastEndDate") LocalDate lastEndDate,
            Pageable pageable);
}
