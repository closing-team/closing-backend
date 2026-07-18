package com.closing.closing.domain.product.repository;

import com.closing.closing.domain.product.entity.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {

    boolean existsByProduct_IdAndUser_Id(Long productId, Long userId);
}
