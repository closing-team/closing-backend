package com.closing.closing.domain.product.repository;

import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndStatusNot(Long productId, ProductStatus status);

}
