package com.closing.closing.domain.product.service;

import com.closing.closing.domain.product.dto.request.ProductCreateRequest;
import com.closing.closing.domain.product.dto.response.ProductCreateResponse;
import com.closing.closing.domain.product.dto.response.ProductResponse;
import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import com.closing.closing.domain.product.entity.TradeMethod;
import com.closing.closing.domain.product.repository.ProductLikeRepository;
import com.closing.closing.domain.product.repository.ProductRepository;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductLikeRepository productLikeRepository;
    private final EntityManager entityManager;

    // productId를 이용해 Product를 찾아 ProductResponse로 만들어 반환하는 함수
    public ProductResponse getProduct(Long productId, Long userId) {

        // TODO: product의 status가 DELETED인 상품 조회되지 않도록 처리
        // Repository 조회 결과는 Optional<Product> 타입, 조회 결과가 없을 수도 있기 때문
        Product product = productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        boolean isBookmarked = productLikeRepository.existsByProduct_IdAndUser_Id(productId, userId);

        boolean isOwner = product.getSeller().getId().equals(userId);

        List<TradeMethod> tradeMethods = product.getTradeMethods();

        return ProductResponse.from(product, tradeMethods, isBookmarked, isOwner);
    }


    @Transactional
    public ProductCreateResponse createProduct(
            Long userId,
            ProductCreateRequest request,
            List<String> imageUrls
    ) {
        // 만약 직거래 가능인데 직거래 장소 없으면 에러
        boolean isDirectTrade = request.getTradeMethods().contains(TradeMethod.DIRECT);
        if (isDirectTrade && !StringUtils.hasText(request.getTradeLocation())) {
            throw new CustomException(ErrorCode.TRADE_LOCATION_REQUIRED);
        }

        User seller = entityManager.getReference(User.class, userId);

        Product product = request.toEntity(seller, imageUrls);

        Product savedProduct = productRepository.save(product);

        return ProductCreateResponse.from(savedProduct);
    }

    @Transactional
    public void deleteProduct(Long productId, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 만약 상품의 판매자 id와 userId가 다르다면 에러
        boolean isOwner = product.getSeller().getId().equals(userId);
        if (!isOwner) {
            throw new CustomException(ErrorCode.PRODUCT_DELETE_FORBIDDEN);
        }

        // 소프트 삭제
        product.delete();
    }
}
