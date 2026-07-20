package com.closing.closing.domain.product.repository;

import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndStatusNot(Long productId, ProductStatus status);

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.seller.id = :userId
              AND p.status <> :deletedStatus
              AND (:status IS NULL OR p.status = :status)
              AND (:cursor IS NULL OR p.id < :cursor)
            ORDER BY p.id DESC
            """)
    List<Product> findMyProducts(
            @Param("userId") Long userId,
            @Param("deletedStatus") ProductStatus deletedStatus,
            @Param("status") ProductStatus status,
            @Param("cursor") Long cursor,
            Pageable pageable
    );
}
