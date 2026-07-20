package com.closing.closing.domain.product.service;

import com.closing.closing.domain.product.dto.request.*;
import com.closing.closing.domain.product.dto.response.*;
import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductBookmark;
import com.closing.closing.domain.product.entity.ProductStatus;
import com.closing.closing.domain.product.entity.TradeMethod;
import com.closing.closing.domain.product.repository.ProductBookmarkRepository;
import com.closing.closing.domain.product.repository.ProductDistanceProjection;
import com.closing.closing.domain.product.repository.ProductRepository;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.closing.closing.global.util.DistanceCalculator;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    // nearby 거리 - 2km
    private static final double NEARBY_RADIUS_KM = 2.0;

    private final ProductRepository productRepository;
    private final ProductBookmarkRepository productBookmarkRepository;
    private final EntityManager entityManager;

    // productId를 이용해 Product를 찾아 ProductResponse로 만들어 반환하는 함수
    public ProductResponse getProduct(
            Long productId,
            Long userId,
            ProductRequest request
    ) {

        // TODO: product의 status가 DELETED인 상품 조회되지 않도록 처리
        // Repository 조회 결과는 Optional<Product> 타입, 조회 결과가 없을 수도 있기 때문
        Product product = productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        boolean isBookmarked = productBookmarkRepository.existsByProduct_IdAndUser_Id(productId, userId);

        boolean isOwner = product.getSeller().getId().equals(userId);

        List<TradeMethod> tradeMethods = product.getTradeMethods();

        Double distanceKm = DistanceCalculator.calculateKm(
                request.getLatitude(),
                request.getLongitude(),
                product.getLatitude(),
                product.getLongitude()
        );

        return ProductResponse.from(
                product,
                tradeMethods,
                isBookmarked,
                isOwner,
                distanceKm
        );
    }


    @Transactional
    public ProductCreateResponse createProduct(
            Long userId,
            ProductCreateRequest request,
            List<String> imageUrls
    ) {
        // 만약 직거래 가능인데 직거래 장소 없으면 에러
        boolean isDirectTrade = request.getTradeMethods().contains(TradeMethod.DIRECT);
        boolean hasInvalidDirectLocation =
                !StringUtils.hasText(request.getTradeLocation())
                        || request.getLatitude() == null
                        || request.getLongitude() == null;

        if (isDirectTrade && hasInvalidDirectLocation) {
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

    public ProductListResponse<ProductSummaryResponse, Long> getMyProducts(
            Long userId,
            MyProductListRequest request
    ) {

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

        Set<Long> bookmarkedProductIds = findBookmarkedProductIds(
                userId,
                pageProducts.stream().map(Product::getId).toList()
        );

        List<ProductSummaryResponse> productResponses =
                pageProducts.stream()
                        .map(product -> ProductSummaryResponse.from(
                                product,
                                null,
                                bookmarkedProductIds.contains(product.getId())
                        ))
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

    public ProductListResponse<ProductSummaryResponse, Long> getBookmarkedProducts(
            Long userId,
            ProductBookmarkListRequest request
    ) {
        Pageable pageable = PageRequest.of(
                0, request.getSize() + 1
        );

        List<ProductBookmark> productBookmarks = productBookmarkRepository.findBookmarkedProducts(
                userId,
                ProductStatus.DELETED,
                request.getCursor(),
                pageable
        );

        boolean hasNext = productBookmarks.size() > request.getSize();
        List<ProductBookmark> pageProductBookmarks = hasNext
                ? productBookmarks.subList(0, request.getSize())
                : productBookmarks;

        List<ProductSummaryResponse> productResponses =
                pageProductBookmarks.stream()
                        .map(ProductBookmark::getProduct)
                        .map(product -> {
                            Double distanceKm =
                                    DistanceCalculator.calculateKm(
                                            request.getLatitude(),
                                            request.getLongitude(),
                                            product.getLatitude(),
                                            product.getLongitude()
                                    );

                            return ProductSummaryResponse.from(
                                    product,
                                    distanceKm,
                                    true
                            );
                        })
                        .toList();

        Long nextCursor =
                hasNext && !pageProductBookmarks.isEmpty()
                        ? pageProductBookmarks
                                .get(pageProductBookmarks.size() - 1)
                                .getId()
                        : null;

        CursorPageResponse<Long> pageResponse =
                CursorPageResponse.of(nextCursor, hasNext);

        return new ProductListResponse<>(
                productResponses,
                pageResponse
        );
    }

    // 최신순 커서 변환 메서드
    private Long parseLatestCursor(String cursor) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }

        try {
            Long cursorId = Long.valueOf(cursor);

            if (cursorId <= 0) {
                throw new CustomException(ErrorCode.INVALID_CURSOR);
            }

            return cursorId;
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_CURSOR);
        }
    }

    private record PriceCursor(Integer price, Long productId) {
    }

    private record PopularCursor(Long bookmarkCount, Long productId) {
    }

    private record DistanceCursor(Double distanceKm, Long productId) {
    }

    private PriceCursor parsePriceCursor(String cursor) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }

        String[] values = cursor.split(":", -1);
        if (values.length != 2) {
            throw new CustomException(ErrorCode.INVALID_CURSOR);
        }

        try {
            Integer price = Integer.valueOf(values[0]);
            Long productId = Long.valueOf(values[1]);

            if (price < 0 || productId <= 0) {
                throw new CustomException(ErrorCode.INVALID_CURSOR);
            }

            return new PriceCursor(price, productId);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_CURSOR);
        }
    }

    private PopularCursor parsePopularCursor(String cursor) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }

        String[] values = cursor.split(":", -1);
        if (values.length != 2) {
            throw new CustomException(ErrorCode.INVALID_CURSOR);
        }

        try {
            Long bookmarkCount = Long.valueOf(values[0]);
            Long productId = Long.valueOf(values[1]);

            if (bookmarkCount < 0 || productId <= 0) {
                throw new CustomException(ErrorCode.INVALID_CURSOR);
            }

            return new PopularCursor(bookmarkCount, productId);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_CURSOR);
        }
    }

    private DistanceCursor parseDistanceCursor(String cursor) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }

        String[] values = cursor.split(":", -1);
        if (values.length != 2) {
            throw new CustomException(ErrorCode.INVALID_CURSOR);
        }

        try {
            Double distanceKm = Double.valueOf(values[0]);
            Long productId = Long.valueOf(values[1]);

            if (!Double.isFinite(distanceKm)
                    || distanceKm < 0
                    || productId <= 0) {
                throw new CustomException(ErrorCode.INVALID_CURSOR);
            }

            return new DistanceCursor(distanceKm, productId);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_CURSOR);
        }
    }

    public ProductListResponse<ProductSummaryResponse, String> getProducts(
            Long userId,
            ProductListRequest request
    ) {
        return switch (request.getSort()) {
            case LATEST -> getLatestProducts(userId, request);
            case CHEAPEST -> getProductsByPrice(userId, request, true);
            case MOST_EXPENSIVE -> getProductsByPrice(userId, request, false);
            case POPULAR -> getPopularProducts(userId, request);
            case NEAREST -> getNearestProducts(userId, request);
        };
    }


    private ProductListResponse<ProductSummaryResponse, String> getLatestProducts(
            Long userId,
            ProductListRequest request
    ) {
        // 최신순 cursor는 상품 ID
        Long cursorId = parseLatestCursor(request.getCursor());

        // 검색어가 비어 있으면 필터를 사용하지 않도록 null로 변경
        String keyword = StringUtils.hasText(request.getKeyword())
                ? request.getKeyword().trim()
                : null;

        List<TradeMethod> tradeMethods = request.getTradeMethods();

        // DIRECT가 포함되면 직거래 가능한 상품만 조회
        boolean requireDirect =
                tradeMethods != null
                        && tradeMethods.contains(TradeMethod.DIRECT);

        // DELIVERY가 포함되면 택배 가능한 상품만 조회
        boolean requireDelivery =
                tradeMethods != null
                        && tradeMethods.contains(TradeMethod.DELIVERY);

        // 다음 페이지 존재 여부 확인을 위해 1개 더 조회
        Pageable pageable = PageRequest.of(
                0,
                request.getSize() + 1
        );

        String businessCategory =
                request.getBusinessCategory() == null
                        ? null
                        : request.getBusinessCategory().name();

        String productCategory =
                request.getProductCategory() == null
                        ? null
                        : request.getProductCategory().name();

        List<Product> products = productRepository.findLatestProducts(
                ProductStatus.DELETED.name(),
                keyword,
                businessCategory,
                productCategory,
                requireDirect,
                requireDelivery,
                request.isNearby(),
                request.getLatitude(),
                request.getLongitude(),
                NEARBY_RADIUS_KM,
                cursorId,
                pageable
        );

        boolean hasNext =
                products.size() > request.getSize();

        List<Product> pageProducts = hasNext
                ? products.subList(0, request.getSize())
                : products;

        Set<Long> bookmarkedProductIds = findBookmarkedProductIds(
                userId,
                pageProducts.stream().map(Product::getId).toList()
        );

        // 사용자의 현재 위치와 상품의 직거래 위치로 거리 계산
        List<ProductSummaryResponse> productResponses =
                pageProducts.stream()
                        .map(product -> {
                            Double distanceKm =
                                    DistanceCalculator.calculateKm(
                                            request.getLatitude(),
                                            request.getLongitude(),
                                            product.getLatitude(),
                                            product.getLongitude()
                                    );

                            return ProductSummaryResponse.from(
                                    product,
                                    distanceKm,
                                    bookmarkedProductIds.contains(product.getId())
                            );
                        })
                        .toList();

        // 최신순 커서는 마지막 상품 ID
        String nextCursor =
                hasNext && !pageProducts.isEmpty()
                        ? String.valueOf(
                        pageProducts
                                .get(pageProducts.size() - 1)
                                .getId()
                )
                        : null;

        CursorPageResponse<String> pageResponse =
                CursorPageResponse.of(nextCursor, hasNext);

        return new ProductListResponse<>(
                productResponses,
                pageResponse
        );
    }

    private ProductListResponse<ProductSummaryResponse, String> getProductsByPrice(
            Long userId,
            ProductListRequest request,
            boolean cheapest
    ) {
        PriceCursor cursor = parsePriceCursor(request.getCursor());
        Integer cursorPrice = cursor == null ? null : cursor.price();
        Long cursorId = cursor == null ? null : cursor.productId();

        String keyword = StringUtils.hasText(request.getKeyword())
                ? request.getKeyword().trim()
                : null;

        List<TradeMethod> tradeMethods = request.getTradeMethods();
        boolean requireDirect = tradeMethods != null
                && tradeMethods.contains(TradeMethod.DIRECT);
        boolean requireDelivery = tradeMethods != null
                && tradeMethods.contains(TradeMethod.DELIVERY);

        String businessCategory = request.getBusinessCategory() == null
                ? null
                : request.getBusinessCategory().name();
        String productCategory = request.getProductCategory() == null
                ? null
                : request.getProductCategory().name();

        Pageable pageable = PageRequest.of(0, request.getSize() + 1);

        List<Product> products = productRepository.findProductsByPrice(
                ProductStatus.DELETED.name(),
                keyword,
                businessCategory,
                productCategory,
                requireDirect,
                requireDelivery,
                request.isNearby(),
                request.getLatitude(),
                request.getLongitude(),
                NEARBY_RADIUS_KM,
                cheapest,
                cursorPrice,
                cursorId,
                pageable
        );

        boolean hasNext = products.size() > request.getSize();
        List<Product> pageProducts = hasNext
                ? products.subList(0, request.getSize())
                : products;

        Set<Long> bookmarkedProductIds = findBookmarkedProductIds(
                userId,
                pageProducts.stream().map(Product::getId).toList()
        );

        List<ProductSummaryResponse> productResponses = pageProducts.stream()
                .map(product -> {
                    Double distanceKm = DistanceCalculator.calculateKm(
                            request.getLatitude(),
                            request.getLongitude(),
                            product.getLatitude(),
                            product.getLongitude()
                    );
                    return ProductSummaryResponse.from(
                            product,
                            distanceKm,
                            bookmarkedProductIds.contains(product.getId())
                    );
                })
                .toList();

        String nextCursor = hasNext && !pageProducts.isEmpty()
                ? createPriceCursor(pageProducts.get(pageProducts.size() - 1))
                : null;

        return new ProductListResponse<>(
                productResponses,
                CursorPageResponse.of(nextCursor, hasNext)
        );
    }

    private String createPriceCursor(Product product) {
        return product.getPrice() + ":" + product.getId();
    }

    private ProductListResponse<ProductSummaryResponse, String> getPopularProducts(
            Long userId,
            ProductListRequest request
    ) {
        PopularCursor cursor = parsePopularCursor(request.getCursor());
        Long cursorBookmarkCount = cursor == null ? null : cursor.bookmarkCount();
        Long cursorId = cursor == null ? null : cursor.productId();

        String keyword = StringUtils.hasText(request.getKeyword())
                ? request.getKeyword().trim()
                : null;

        List<TradeMethod> tradeMethods = request.getTradeMethods();
        boolean requireDirect = tradeMethods != null
                && tradeMethods.contains(TradeMethod.DIRECT);
        boolean requireDelivery = tradeMethods != null
                && tradeMethods.contains(TradeMethod.DELIVERY);

        String businessCategory = request.getBusinessCategory() == null
                ? null
                : request.getBusinessCategory().name();
        String productCategory = request.getProductCategory() == null
                ? null
                : request.getProductCategory().name();

        Pageable pageable = PageRequest.of(0, request.getSize() + 1);

        List<Product> products = productRepository.findPopularProducts(
                ProductStatus.DELETED.name(),
                keyword,
                businessCategory,
                productCategory,
                requireDirect,
                requireDelivery,
                request.isNearby(),
                request.getLatitude(),
                request.getLongitude(),
                NEARBY_RADIUS_KM,
                cursorBookmarkCount,
                cursorId,
                pageable
        );

        boolean hasNext = products.size() > request.getSize();
        List<Product> pageProducts = hasNext
                ? products.subList(0, request.getSize())
                : products;

        Set<Long> bookmarkedProductIds = findBookmarkedProductIds(
                userId,
                pageProducts.stream().map(Product::getId).toList()
        );

        List<ProductSummaryResponse> productResponses = pageProducts.stream()
                .map(product -> {
                    Double distanceKm = DistanceCalculator.calculateKm(
                            request.getLatitude(),
                            request.getLongitude(),
                            product.getLatitude(),
                            product.getLongitude()
                    );
                    return ProductSummaryResponse.from(
                            product,
                            distanceKm,
                            bookmarkedProductIds.contains(product.getId())
                    );
                })
                .toList();

        String nextCursor = null;
        if (hasNext && !pageProducts.isEmpty()) {
            Product lastProduct = pageProducts.get(pageProducts.size() - 1);
            long bookmarkCount = productBookmarkRepository.countByProduct_Id(
                    lastProduct.getId()
            );
            nextCursor = bookmarkCount + ":" + lastProduct.getId();
        }

        return new ProductListResponse<>(
                productResponses,
                CursorPageResponse.of(nextCursor, hasNext)
        );
    }

    private ProductListResponse<ProductSummaryResponse, String> getNearestProducts(
            Long userId,
            ProductListRequest request
    ) {
        DistanceCursor cursor = parseDistanceCursor(request.getCursor());
        Double cursorDistance = cursor == null ? null : cursor.distanceKm();
        Long cursorId = cursor == null ? null : cursor.productId();

        String keyword = StringUtils.hasText(request.getKeyword())
                ? request.getKeyword().trim()
                : null;

        List<TradeMethod> tradeMethods = request.getTradeMethods();
        boolean requireDirect = tradeMethods != null
                && tradeMethods.contains(TradeMethod.DIRECT);
        boolean requireDelivery = tradeMethods != null
                && tradeMethods.contains(TradeMethod.DELIVERY);

        String businessCategory = request.getBusinessCategory() == null
                ? null
                : request.getBusinessCategory().name();
        String productCategory = request.getProductCategory() == null
                ? null
                : request.getProductCategory().name();

        Pageable pageable = PageRequest.of(0, request.getSize() + 1);

        List<ProductDistanceProjection> results =
                productRepository.findNearestProducts(
                        ProductStatus.DELETED.name(),
                        keyword,
                        businessCategory,
                        productCategory,
                        requireDirect,
                        requireDelivery,
                        request.isNearby(),
                        request.getLatitude(),
                        request.getLongitude(),
                        NEARBY_RADIUS_KM,
                        cursorDistance,
                        cursorId,
                        pageable
                );

        boolean hasNext = results.size() > request.getSize();
        List<ProductDistanceProjection> pageResults = hasNext
                ? results.subList(0, request.getSize())
                : results;

        List<Long> productIds = pageResults.stream()
                .map(ProductDistanceProjection::getProductId)
                .toList();

        Set<Long> bookmarkedProductIds = findBookmarkedProductIds(
                userId,
                productIds
        );

        Map<Long, Product> productsById = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<ProductSummaryResponse> productResponses = pageResults.stream()
                .map(result -> {
                    Product product = productsById.get(result.getProductId());
                    double roundedDistance =
                            Math.round(result.getDistanceKm() * 10.0) / 10.0;

                    return ProductSummaryResponse.from(
                            product,
                            roundedDistance,
                            bookmarkedProductIds.contains(product.getId())
                    );
                })
                .toList();

        String nextCursor = null;
        if (hasNext && !pageResults.isEmpty()) {
            ProductDistanceProjection lastResult =
                    pageResults.get(pageResults.size() - 1);
            nextCursor = lastResult.getDistanceKm()
                    + ":"
                    + lastResult.getProductId();
        }

        return new ProductListResponse<>(
                productResponses,
                CursorPageResponse.of(nextCursor, hasNext)
        );
    }

    private Set<Long> findBookmarkedProductIds(
            Long userId,
            List<Long> productIds
    ) {
        if (userId == null || productIds.isEmpty()) {
            return Set.of();
        }

        return productBookmarkRepository.findBookmarkedProductIds(
                userId,
                productIds
        );
    }

}
