package com.closing.closing.domain.product.service;

import com.closing.closing.domain.product.dto.ProductResponse;
import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.repository.ProductRepository;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    // productId를 이용해 Product를 찾아 ProductResponse로 만들어 반환하는 함수
    public ProductResponse getProduct(Long productId) {

        // Repository 조회 결과는 Optional<Product> 타입, 조회 결과가 없을 수도 있기 때문
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        return ProductResponse.from(product);
    }
}
