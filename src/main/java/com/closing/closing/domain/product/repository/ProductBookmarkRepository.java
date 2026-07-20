package com.closing.closing.domain.product.repository;

import com.closing.closing.domain.product.entity.ProductBookmark;
import com.closing.closing.domain.product.entity.ProductStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductBookmarkRepository extends JpaRepository<ProductBookmark, Long> {

    boolean existsByProduct_IdAndUser_Id(Long productId, Long userId);

    Optional<ProductBookmark> findByProduct_IdAndUser_Id(Long productId, Long userId);

    long countByProduct_Id(Long productId);

    @Query("""
            SELECT b.product.id
            FROM ProductBookmark b
            WHERE b.user.id = :userId
              AND b.product.id IN :productIds
            """)
    Set<Long> findBookmarkedProductIds(
            @Param("userId") Long userId,
            @Param("productIds") List<Long> productIds
    );

    @Query("""
        SELECT b
        FROM ProductBookmark b
        JOIN FETCH b.product p
        WHERE b.user.id = :userId
          AND p.status <> :deletedStatus
          AND (:cursor IS NULL OR b.id < :cursor)
        ORDER BY b.id DESC
        """)
    List<ProductBookmark> findBookmarkedProducts(
            @Param("userId") Long userId,
            @Param("deletedStatus") ProductStatus deletedStatus,
            @Param("cursor") Long cursor,
            Pageable pageable
    );
}
