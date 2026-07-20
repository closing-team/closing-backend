package com.closing.closing.domain.product.repository;

import com.closing.closing.domain.product.entity.ProductBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductBookmarkRepository extends JpaRepository<ProductBookmark, Long> {

    boolean existsByProduct_IdAndUser_Id(Long productId, Long userId);

    Optional<ProductBookmark> findByProduct_IdAndUser_Id(Long productId, Long userId);
}
