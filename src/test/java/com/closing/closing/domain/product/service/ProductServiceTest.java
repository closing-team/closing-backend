package com.closing.closing.domain.product.service;

import com.closing.closing.domain.product.dto.request.*;
import com.closing.closing.domain.product.dto.response.ProductListResponse;
import com.closing.closing.domain.product.dto.response.MyProductListResponse;
import com.closing.closing.domain.product.dto.response.ProductSummaryResponse;
import com.closing.closing.domain.product.dto.response.ProductUpdateResponse;
import com.closing.closing.domain.product.entity.*;
import com.closing.closing.domain.product.repository.ProductBookmarkCountProjection;
import com.closing.closing.domain.product.repository.ProductBookmarkRepository;
import com.closing.closing.domain.product.repository.ProductRepository;
import com.closing.closing.domain.product.repository.ProductStatusCountProjection;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductBookmarkRepository productBookmarkRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private ProductImageService productImageService;

    @InjectMocks
    private ProductService productService;

    private User seller;

    @BeforeEach
    void setUp() {
        seller = createUser(USER_ID, "판매자");
    }

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("상품 상세 조회 시 찜·소유 여부와 거리 정보를 반환한다")
    void getProduct_success() {
        // 상품 상세 조회의 응답 조립과 사용자별 상태를 검증한다.
        Product product = createProduct(PRODUCT_ID, seller, List.of("old.jpg"));
        ProductRequest request = new ProductRequest();
        request.setLatitude(new BigDecimal("37.5665"));
        request.setLongitude(new BigDecimal("126.9780"));

        given(productRepository.findByIdAndStatusNot(PRODUCT_ID, ProductStatus.DELETED))
                .willReturn(Optional.of(product));
        given(productBookmarkRepository.existsByProduct_IdAndUser_Id(PRODUCT_ID, USER_ID))
                .willReturn(true);

        var response = productService.getProduct(PRODUCT_ID, USER_ID, request);

        assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(response.isBookmarked()).isTrue();
        assertThat(response.isOwner()).isTrue();
        assertThat(response.getTradeLocation()).isNotNull();
        assertThat(response.getSeller().getMemberId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("존재하지 않거나 삭제된 상품 상세 조회는 실패한다")
    void getProduct_notFound() {
        // 삭제 상품을 일반 상세 조회에서 제외하는지 검증한다.
        given(productRepository.findByIdAndStatusNot(PRODUCT_ID, ProductStatus.DELETED))
                .willReturn(Optional.empty());

        assertErrorCode(
                () -> productService.getProduct(PRODUCT_ID, USER_ID, new ProductRequest()),
                ErrorCode.PRODUCT_NOT_FOUND
        );
    }

    @Test
    @DisplayName("상품 등록 시 인증 사용자와 요청값으로 상품을 저장한다")
    void createProduct_success() {
        // 등록 요청이 Product 엔티티로 변환되어 저장되는지 검증한다.
        ProductCreateRequest request = createProductRequest(true);
        List<String> imageUrls = List.of("one.jpg", "two.jpg");
        given(entityManager.getReference(User.class, USER_ID)).willReturn(seller);
        given(productRepository.save(any(Product.class))).willAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", PRODUCT_ID);
            ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.of(2026, 7, 21, 12, 0));
            return saved;
        });

        var response = productService.createProduct(USER_ID, request, imageUrls);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getSeller()).isSameAs(seller);
        assertThat(captor.getValue().getTradeMethods())
                .containsExactly(TradeMethod.DIRECT, TradeMethod.DELIVERY);
        assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(response.getImageUrls()).containsExactlyElementsOf(imageUrls);
    }

    @Test
    @DisplayName("직거래 상품 등록에 거래 위치가 없으면 실패한다")
    void createProduct_directLocationRequired() {
        // 직거래 선택 시 주소와 좌표가 필수인지 검증한다.
        ProductCreateRequest request = createProductRequest(true);
        ReflectionTestUtils.setField(request, "tradeLocation", null);

        assertErrorCode(
                () -> productService.createProduct(USER_ID, request, List.of("one.jpg")),
                ErrorCode.TRADE_LOCATION_REQUIRED
        );
        verifyNoInteractions(entityManager, productRepository);
    }

    @Test
    @DisplayName("판매자는 상품을 소프트 삭제할 수 있다")
    void deleteProduct_success() {
        // 삭제 시 DB 행을 지우지 않고 상태만 DELETED로 변경하는지 검증한다.
        Product product = createProduct(PRODUCT_ID, seller, List.of("one.jpg"));
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));

        productService.deleteProduct(PRODUCT_ID, USER_ID);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.DELETED);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    @DisplayName("판매자가 아닌 사용자는 상품을 삭제할 수 없다")
    void deleteProduct_forbidden() {
        // 상품 삭제의 판매자 권한 검증을 확인한다.
        Product product = createProduct(PRODUCT_ID, seller, List.of("one.jpg"));
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));

        assertErrorCode(
                () -> productService.deleteProduct(PRODUCT_ID, 999L),
                ErrorCode.PRODUCT_DELETE_FORBIDDEN
        );
        assertThat(product.getStatus()).isEqualTo(ProductStatus.SELLING);
    }

    @Test
    @DisplayName("판매자는 상품 거래 상태를 변경할 수 있다")
    void updateProductStatus_success() {
        // 문자열 상태가 ProductStatus로 변환되어 반영되는지 검증한다.
        Product product = createProduct(PRODUCT_ID, seller, List.of("one.jpg"));
        given(productRepository.findByIdAndStatusNot(PRODUCT_ID, ProductStatus.DELETED))
                .willReturn(Optional.of(product));

        var response = productService.updateProductStatus(USER_ID, PRODUCT_ID, "SOLD_OUT");

        assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        assertThat(response.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        verify(productRepository).flush();
    }

    @Test
    @DisplayName("지원하지 않는 상태 또는 DELETED 상태 변경은 실패한다")
    void updateProductStatus_invalidStatus() {
        // 삭제 전용 상태와 잘못된 문자열이 상태 변경 API에서 차단되는지 검증한다.
        assertErrorCode(
                () -> productService.updateProductStatus(USER_ID, PRODUCT_ID, "DELETED"),
                ErrorCode.INVALID_PRODUCT_STATUS
        );
        assertErrorCode(
                () -> productService.updateProductStatus(USER_ID, PRODUCT_ID, "UNKNOWN"),
                ErrorCode.INVALID_PRODUCT_STATUS
        );
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("상품 찜을 새로 등록한다")
    void createProductBookmark_success() {
        // 찜이 없을 때 사용자와 상품을 연결한 엔티티가 저장되는지 검증한다.
        Product product = createProduct(PRODUCT_ID, seller, List.of("one.jpg"));
        given(productBookmarkRepository.existsByProduct_IdAndUser_Id(PRODUCT_ID, USER_ID))
                .willReturn(false);
        given(productRepository.findByIdAndStatusNot(PRODUCT_ID, ProductStatus.DELETED))
                .willReturn(Optional.of(product));
        given(entityManager.getReference(User.class, USER_ID)).willReturn(seller);

        var response = productService.createProductBookmark(USER_ID, PRODUCT_ID);

        ArgumentCaptor<ProductBookmark> captor = ArgumentCaptor.forClass(ProductBookmark.class);
        verify(productBookmarkRepository).save(captor.capture());
        assertThat(captor.getValue().getProduct()).isSameAs(product);
        assertThat(response.isBookmarked()).isTrue();
    }

    @Test
    @DisplayName("이미 찜한 상품의 찜 추가 요청은 중복 저장하지 않는다")
    void createProductBookmark_idempotent() {
        // 동일 상품에 대한 중복 찜 생성을 방지하는지 검증한다.
        Product product = createProduct(PRODUCT_ID, seller, List.of("one.jpg"));
        given(productBookmarkRepository.existsByProduct_IdAndUser_Id(PRODUCT_ID, USER_ID))
                .willReturn(true);
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));

        var response = productService.createProductBookmark(USER_ID, PRODUCT_ID);

        assertThat(response.isBookmarked()).isTrue();
        verify(productBookmarkRepository, never()).save(any());
    }

    @Test
    @DisplayName("등록된 상품 찜을 취소한다")
    void deleteProductBookmark_success() {
        // 사용자에게 속한 찜 엔티티만 삭제되는지 검증한다.
        Product product = createProduct(PRODUCT_ID, seller, List.of("one.jpg"));
        ProductBookmark bookmark = ProductBookmark.builder().product(product).user(seller).build();
        given(productRepository.findByIdAndStatusNot(PRODUCT_ID, ProductStatus.DELETED))
                .willReturn(Optional.of(product));
        given(productBookmarkRepository.findByProduct_IdAndUser_Id(PRODUCT_ID, USER_ID))
                .willReturn(Optional.of(bookmark));

        var response = productService.deleteProductBookmark(USER_ID, PRODUCT_ID);

        verify(productBookmarkRepository).delete(bookmark);
        assertThat(response.isBookmarked()).isFalse();
    }

    @Test
    @DisplayName("내 상품 조회는 요청 크기만 반환하고 다음 커서를 만든다")
    void getMyProducts_cursorPaging() {
        // size보다 한 건 더 조회해 hasNext와 nextCursor를 계산하는지 검증한다.
        Product first = createProduct(30L, seller, List.of("one.jpg"));
        Product second = createProduct(20L, seller, List.of("two.jpg"));
        Product extra = createProduct(10L, seller, List.of("three.jpg"));
        MyProductListRequest request = new MyProductListRequest();
        request.setSize(2);
        request.setStatus("SELLING");
        given(productRepository.findMyProducts(
                eq(USER_ID), eq(ProductStatus.DELETED), eq(ProductStatus.SELLING),
                isNull(), any(Pageable.class)
        )).willReturn(List.of(first, second, extra));
        given(productBookmarkRepository.findBookmarkedProductIds(USER_ID, List.of(30L, 20L)))
                .willReturn(Set.of(20L));

        ProductBookmarkCountProjection firstBookmarkCount =
                mock(ProductBookmarkCountProjection.class);
        ProductBookmarkCountProjection secondBookmarkCount =
                mock(ProductBookmarkCountProjection.class);
        given(firstBookmarkCount.getProductId()).willReturn(30L);
        given(firstBookmarkCount.getBookmarkCount()).willReturn(3L);
        given(secondBookmarkCount.getProductId()).willReturn(20L);
        given(secondBookmarkCount.getBookmarkCount()).willReturn(2L);
        given(productBookmarkRepository.findBookmarkCounts(List.of(30L, 20L)))
                .willReturn(List.of(firstBookmarkCount, secondBookmarkCount));

        ProductStatusCountProjection sellingCount =
                mock(ProductStatusCountProjection.class);
        ProductStatusCountProjection soldOutCount =
                mock(ProductStatusCountProjection.class);
        given(sellingCount.getStatus()).willReturn(ProductStatus.SELLING);
        given(sellingCount.getProductCount()).willReturn(2L);
        given(soldOutCount.getStatus()).willReturn(ProductStatus.SOLD_OUT);
        given(soldOutCount.getProductCount()).willReturn(1L);
        given(productRepository.findMyProductCounts(USER_ID, ProductStatus.DELETED))
                .willReturn(List.of(sellingCount, soldOutCount));

        MyProductListResponse response =
                productService.getMyProducts(USER_ID, request);

        assertThat(response.getProducts()).hasSize(2);
        assertThat(response.getPage().isHasNext()).isTrue();
        assertThat(response.getPage().getNextCursor()).isEqualTo(20L);
        assertThat(response.getProducts().get(1).isBookmarked()).isTrue();
        assertThat(response.getProducts().get(0).getBookmarkCount()).isEqualTo(3L);
        assertThat(response.getCounts().getTotal()).isEqualTo(3L);
        assertThat(response.getCounts().getSelling()).isEqualTo(2L);
        assertThat(response.getCounts().getReserved()).isZero();
        assertThat(response.getCounts().getSoldOut()).isEqualTo(1L);
    }

    @Test
    @DisplayName("찜 상품 조회는 찜 ID를 커서로 사용한다")
    void getBookmarkedProducts_cursorPaging() {
        // 찜 목록의 다음 커서와 모든 상품의 찜 상태를 검증한다.
        Product first = createProduct(30L, seller, List.of("one.jpg"));
        Product second = createProduct(20L, seller, List.of("two.jpg"));
        Product extra = createProduct(10L, seller, List.of("three.jpg"));
        ProductBookmark firstBookmark = createBookmark(300L, first);
        ProductBookmark secondBookmark = createBookmark(200L, second);
        ProductBookmark extraBookmark = createBookmark(100L, extra);
        ProductBookmarkListRequest request = new ProductBookmarkListRequest();
        request.setSize(2);
        request.setLatitude(new BigDecimal("37.5665"));
        request.setLongitude(new BigDecimal("126.9780"));
        given(productBookmarkRepository.findBookmarkedProducts(
                eq(USER_ID), eq(ProductStatus.DELETED), isNull(), any(Pageable.class)
        )).willReturn(List.of(firstBookmark, secondBookmark, extraBookmark));

        ProductListResponse<ProductSummaryResponse, Long> response =
                productService.getBookmarkedProducts(USER_ID, request);

        assertThat(response.getProducts()).hasSize(2);
        assertThat(response.getProducts()).allMatch(ProductSummaryResponse::isBookmarked);
        assertThat(response.getPage().getNextCursor()).isEqualTo(200L);
        assertThat(response.getPage().isHasNext()).isTrue();
    }

    @Test
    @DisplayName("상품 목록 최신순 조회는 필터와 커서 페이징을 적용한다")
    void getProducts_latestCursorPaging() {
        // 최신순 목록에서 필터 전달, 찜 상태, 다음 커서를 함께 검증한다.
        Product first = createProduct(30L, seller, List.of("one.jpg"));
        Product second = createProduct(20L, seller, List.of("two.jpg"));
        Product extra = createProduct(10L, seller, List.of("three.jpg"));
        ProductListRequest request = createProductListRequest();
        request.setSize(2);
        request.setKeyword("  의자  ");
        request.setTradeMethods(List.of(TradeMethod.DIRECT));
        request.setBusinessCategory(BusinessCategory.KOREAN_MEAL);
        request.setProductCategory(ProductCategory.CHAIR_SOFA_BAR_CHAIR);
        given(productRepository.findLatestProducts(
                eq("DELETED"), eq("의자"), eq("KOREAN_MEAL"),
                eq("CHAIR_SOFA_BAR_CHAIR"), eq(true), eq(false), eq(false),
                any(BigDecimal.class), any(BigDecimal.class), eq(2.0), isNull(),
                any(Pageable.class)
        )).willReturn(List.of(first, second, extra));
        given(productBookmarkRepository.findBookmarkedProductIds(USER_ID, List.of(30L, 20L)))
                .willReturn(Set.of(30L));

        ProductListResponse<ProductSummaryResponse, String> response =
                productService.getProducts(USER_ID, request);

        assertThat(response.getProducts()).hasSize(2);
        assertThat(response.getProducts().get(0).isBookmarked()).isTrue();
        assertThat(response.getPage().getNextCursor()).isEqualTo("20");
        assertThat(response.getPage().isHasNext()).isTrue();
    }

    @Test
    @DisplayName("정렬 방식에 맞지 않는 상품 목록 커서는 거부한다")
    void getProducts_invalidCursor() {
        // 잘못된 커서가 Repository 호출 전에 차단되는지 검증한다.
        ProductListRequest request = createProductListRequest();
        request.setSort(SortMethod.CHEAPEST);
        request.setCursor("wrong-cursor");

        assertErrorCode(
                () -> productService.getProducts(USER_ID, request),
                ErrorCode.INVALID_CURSOR
        );
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("상품 수정 성공 후 제거한 기존 이미지를 삭제한다")
    void updateProduct_success_deletesRemovedImagesAfterCommit() {
        // DB 커밋 후에만 제거 대상 S3 이미지가 삭제되는지 검증한다.
        Product product = createProduct(PRODUCT_ID, seller, List.of("keep.jpg", "remove.jpg"));
        ProductUpdateRequest request = createUpdateRequest(List.of("keep.jpg"));
        List<MultipartFile> newImages = List.of(
                new MockMultipartFile("newImages", "new.jpg", "image/jpeg", new byte[]{1})
        );
        given(productRepository.findByIdAndStatusNot(PRODUCT_ID, ProductStatus.DELETED))
                .willReturn(Optional.of(product));
        given(productImageService.upload(newImages)).willReturn(List.of("new.jpg"));
        TransactionSynchronizationManager.initSynchronization();

        ProductUpdateResponse response =
                productService.updateProduct(USER_ID, PRODUCT_ID, request, newImages);

        assertThat(response.getImageUrls()).containsExactly("keep.jpg", "new.jpg");
        verify(productImageService, never()).deleteSafely(List.of("remove.jpg"));
        TransactionSynchronization synchronization =
                TransactionSynchronizationManager.getSynchronizations().get(0);
        synchronization.afterCommit();
        synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        verify(productImageService).deleteSafely(List.of("remove.jpg"));
        verify(productImageService, never()).deleteSafely(List.of("new.jpg"));
    }

    @Test
    @DisplayName("상품 수정 트랜잭션 실패 시 새로 업로드한 이미지를 삭제한다")
    void updateProduct_rollback_deletesNewImages() {
        // DB 롤백 때 새 S3 이미지만 보상 삭제되는지 검증한다.
        Product product = createProduct(PRODUCT_ID, seller, List.of("keep.jpg"));
        ProductUpdateRequest request = createUpdateRequest(List.of("keep.jpg"));
        List<MultipartFile> newImages = List.of(
                new MockMultipartFile("newImages", "new.jpg", "image/jpeg", new byte[]{1})
        );
        given(productRepository.findByIdAndStatusNot(PRODUCT_ID, ProductStatus.DELETED))
                .willReturn(Optional.of(product));
        given(productImageService.upload(newImages)).willReturn(List.of("new.jpg"));
        TransactionSynchronizationManager.initSynchronization();

        productService.updateProduct(USER_ID, PRODUCT_ID, request, newImages);
        TransactionSynchronization synchronization =
                TransactionSynchronizationManager.getSynchronizations().get(0);
        synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);

        verify(productImageService).deleteSafely(List.of("new.jpg"));
    }

    @Test
    @DisplayName("다른 판매자의 상품은 수정할 수 없다")
    void updateProduct_forbidden() {
        // 상품 수정의 판매자 권한 검증을 확인한다.
        Product product = createProduct(PRODUCT_ID, seller, List.of("keep.jpg"));
        given(productRepository.findByIdAndStatusNot(PRODUCT_ID, ProductStatus.DELETED))
                .willReturn(Optional.of(product));

        assertErrorCode(
                () -> productService.updateProduct(999L, PRODUCT_ID,
                        createUpdateRequest(List.of("keep.jpg")), List.of()),
                ErrorCode.PRODUCT_UPDATE_FORBIDDEN
        );
        verifyNoInteractions(productImageService);
    }

    @Test
    @DisplayName("기존 상품에 없는 이미지 URL은 유지할 수 없다")
    void updateProduct_invalidRetainedImage() {
        // 다른 상품의 S3 URL을 유지 이미지로 위조할 수 없도록 검증한다.
        Product product = createProduct(PRODUCT_ID, seller, List.of("keep.jpg"));
        given(productRepository.findByIdAndStatusNot(PRODUCT_ID, ProductStatus.DELETED))
                .willReturn(Optional.of(product));

        assertErrorCode(
                () -> productService.updateProduct(USER_ID, PRODUCT_ID,
                        createUpdateRequest(List.of("other.jpg")), List.of()),
                ErrorCode.INVALID_RETAINED_IMAGES
        );
    }

    private Product createProduct(Long id, User user, List<String> imageUrls) {
        Product product = Product.builder()
                .seller(user)
                .title("중고 의자")
                .businessCategory(BusinessCategory.KOREAN_MEAL)
                .productCategory(ProductCategory.CHAIR_SOFA_BAR_CHAIR)
                .price(100_000)
                .description("상태가 좋은 의자입니다.")
                .imageUrls(imageUrls)
                .isDirectAvailable(true)
                .isDeliveryAvailable(true)
                .tradeLocation("서울시 중구")
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .build();
        ReflectionTestUtils.setField(product, "id", id);
        ReflectionTestUtils.setField(product, "createdAt", LocalDateTime.of(2026, 7, 21, 10, 0));
        ReflectionTestUtils.setField(product, "updatedAt", LocalDateTime.of(2026, 7, 21, 11, 0));
        return product;
    }

    private User createUser(Long id, String nickname) {
        User user = User.builder().kakaoId("kakao-" + id).nickname(nickname).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private ProductBookmark createBookmark(Long id, Product product) {
        ProductBookmark bookmark = ProductBookmark.builder().product(product).user(seller).build();
        ReflectionTestUtils.setField(bookmark, "id", id);
        return bookmark;
    }

    private ProductCreateRequest createProductRequest(boolean direct) {
        ProductCreateRequest request = BeanUtils.instantiateClass(ProductCreateRequest.class);
        ReflectionTestUtils.setField(request, "title", "중고 의자");
        ReflectionTestUtils.setField(request, "businessCategory", BusinessCategory.KOREAN_MEAL);
        ReflectionTestUtils.setField(request, "productCategory", ProductCategory.CHAIR_SOFA_BAR_CHAIR);
        ReflectionTestUtils.setField(request, "price", 100_000);
        ReflectionTestUtils.setField(request, "tradeMethods", direct
                ? List.of(TradeMethod.DIRECT, TradeMethod.DELIVERY)
                : List.of(TradeMethod.DELIVERY));
        ReflectionTestUtils.setField(request, "tradeLocation", direct ? "서울시 중구" : null);
        ReflectionTestUtils.setField(request, "description", "상태가 좋은 의자입니다.");
        ReflectionTestUtils.setField(request, "latitude", direct ? new BigDecimal("37.5665") : null);
        ReflectionTestUtils.setField(request, "longitude", direct ? new BigDecimal("126.9780") : null);
        return request;
    }

    private ProductUpdateRequest createUpdateRequest(List<String> retainedImages) {
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setTitle("수정된 의자");
        request.setBusinessCategory(BusinessCategory.OFFICE_FURNITURE_SET);
        request.setProductCategory(ProductCategory.OFFICE_DESK_CHAIR);
        request.setPrice(120_000);
        request.setTradeMethods(List.of(TradeMethod.DIRECT));
        request.setTradeLocation("서울시 종로구");
        request.setLatitude(new BigDecimal("37.5700"));
        request.setLongitude(new BigDecimal("126.9800"));
        request.setDescription("수정된 설명입니다.");
        request.setRetainedImages(retainedImages);
        return request;
    }

    private ProductListRequest createProductListRequest() {
        ProductListRequest request = new ProductListRequest();
        request.setLatitude(new BigDecimal("37.5665"));
        request.setLongitude(new BigDecimal("126.9780"));
        return request;
    }

    private void assertErrorCode(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(CustomException.class)
                .extracting(exception -> ((CustomException) exception).getErrorCode())
                .isEqualTo(expected);
    }
}
