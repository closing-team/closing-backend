package com.closing.closing.domain.product.repository;

import com.closing.closing.domain.product.entity.ProductStatus;

public interface ProductStatusCountProjection {

    ProductStatus getStatus();

    Long getProductCount();
}
