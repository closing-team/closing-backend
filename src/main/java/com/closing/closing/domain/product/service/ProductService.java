package com.closing.closing.domain.product.service;

import com.closing.closing.domain.product.dto.request.MyProductListRequest;
import com.closing.closing.domain.product.dto.request.ProductCreateRequest;
import com.closing.closing.domain.product.dto.response.*;
import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductBookmark;
import com.closing.closing.domain.product.entity.ProductStatus;
import com.closing.closing.domain.product.entity.TradeMethod;
import com.closing.closing.domain.product.repository.ProductBookmarkRepository;
import com.closing.closing.domain.product.repository.ProductRepository;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductBookmarkRepository productBookmarkRepository;
    private final EntityManager entityManager;

    // productId를 이용해 Product를 찾아 ProductResponse로 만들어 반환하는 함수
    public ProductResponse getProduct(Long productId, Long userId) {

        // TODO: product의 status가 DELETED인 상품 조회되지 않도록 처리
        // Repository 조회 결과는 Optional<Product> 타입, 조회 결과가 없을 수도 있기 때문
        Product product = productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        boolean isBookmarked = productBookmarkRepository.existsByProduct_IdAndUser_Id(productId, userId);

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

    @Transactional
    public ProductStatusResponse updateProductStatus(Long userId, Long productId, String status) {

        ProductStatus productStatus = convertProductStatus(status);

        Product product = productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        boolean isOwner = product.getSeller().getId().equals(userId);
        if (!isOwner) {
            throw new CustomException(ErrorCode.PRODUCT_STATUS_UPDATE_FORBIDDEN);
        }

        product.updateStatus(productStatus);
        productRepository.flush(); // 상태변경 SQL을 즉시 DB에 반영 -> 갱신된 updatedAt 값 읽기 위함

        return ProductStatusResponse.from(product);
    }

    // String -> ProductStatus 변환 함수
    private ProductStatus convertProductStatus(String status) {
        try {
            ProductStatus productStatus = ProductStatus.valueOf(status);

            if (productStatus == ProductStatus.DELETED) {
                throw new CustomException(ErrorCode.INVALID_PRODUCT_STATUS);
            }

            return productStatus;
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_PRODUCT_STATUS);
        }
    }

    private ProductStatus convertOptionalProductStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }

        return convertProductStatus(status);
    }

    @Transactional
    public ProductBookmarkResponse createProductBookmark(Long userId, Long productId) {

        // 유니크 제약조건에 따라 이미 찜 되어있는지 확인
        boolean alreadyBookmarked =
                productBookmarkRepository.existsByProduct_IdAndUser_Id(productId, userId);
        if (alreadyBookmarked) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

            return ProductBookmarkResponse.from(product, true);
        }

        Product product = productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        User user = entityManager.getReference(User.class, userId);

        ProductBookmark productBookmark = new ProductBookmark(product, user);
        productBookmarkRepository.save(productBookmark);

        return ProductBookmarkResponse.from(product, true);
    }

    @Transactional
    public ProductBookmarkResponse deleteProductBookmark(Long userId, Long productId) {

        Product product = productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductBookmark productBookmark = productBookmarkRepository.findByProduct_IdAndUser_Id(productId, userId)
                        .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_BOOKMARK_NOT_FOUND));

        productBookmarkRepository.delete(productBookmark);

        return ProductBookmarkResponse.from(product, false);
    }

    public ProductListResponse<ProductSummaryResponse, Long> getMyProducts(Long userId, MyProductListRequest request) {

        // 요청으로 들어온 status 문자열을 ProductStatus로 변환
        ProductStatus status = convertOptionalProductStatus(request.getStatus());

        // hasNext 계산 위해 요청 개수보다 1개 더 조회
        Pageable pageable = PageRequest.of(
                0, request.getSize() + 1
        );

        List<Product> products = productRepository.findMyProducts(
                userId,
                ProductStatus.DELETED,
                status,
                request.getCursor(),
                pageable
        );

        boolean hasNext = products.size() > request.getSize();

        List<Product> pageProducts = hasNext
                ? products.subList(0, request.getSize())
                : products;

        List<ProductSummaryResponse> productResponses = pageProducts.stream()
                .map(ProductSummaryResponse::from)
                .toList();

        Long nextCursor = hasNext && !pageProducts.isEmpty()
                ? pageProducts.get(pageProducts.size() - 1).getId()
                : null;

        CursorPageResponse<Long> pageResponse =
                CursorPageResponse.of(nextCursor, hasNext);

        return new ProductListResponse<>(
                productResponses,
                pageResponse
        );

    }

}
