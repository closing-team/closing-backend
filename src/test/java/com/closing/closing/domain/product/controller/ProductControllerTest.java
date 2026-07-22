package com.closing.closing.domain.product.controller;

import com.closing.closing.domain.product.dto.request.*;
import com.closing.closing.domain.product.dto.response.*;
import com.closing.closing.domain.product.entity.ProductStatus;
import com.closing.closing.domain.product.entity.SortMethod;
import com.closing.closing.domain.product.service.ProductImageService;
import com.closing.closing.domain.product.service.ProductService;
import com.closing.closing.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;
    @Mock
    private ProductImageService productImageService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ProductController controller = new ProductController(productService, productImageService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    @DisplayName("GET /api/v1/products 요청의 조회 조건을 Service에 전달한다")
    void getProducts_success() throws Exception {
        // 상품 목록의 query string 바인딩과 공통 응답 형식을 검증한다.
        ProductListResponse<ProductSummaryResponse, String> response =
                new ProductListResponse<>(List.of(), CursorPageResponse.of(null, false));
        given(productService.getProducts(eq(1L), any(ProductListRequest.class)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/products")
                        .param("sort", SortMethod.LATEST.name())
                        .param("size", "20")
                        .param("latitude", "37.5665")
                        .param("longitude", "126.9780"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.data.products").isArray())
                .andExpect(jsonPath("$.data.page.hasNext").value(false));

        verify(productService).getProducts(eq(1L), argThat(request ->
                request.getSort() == SortMethod.LATEST
                        && request.getSize() == 20
                        && request.getLatitude().compareTo(new BigDecimal("37.5665")) == 0
        ));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} 요청으로 상품 상세를 조회한다")
    void getProduct_success() throws Exception {
        // 상세 조회 path variable과 현재 좌표가 전달되는지 검증한다.
        ProductResponse response = new ProductResponse(
                10L, "중고 의자", 100_000, "설명", List.of("one.jpg"),
                null, null, null, null, List.of(), null, ProductStatus.SELLING,
                false, true, new SellerResponse(1L, "판매자", null),
                LocalDateTime.of(2026, 7, 21, 10, 0)
        );
        given(productService.getProduct(eq(10L), eq(1L), any(ProductRequest.class)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/products/10")
                        .param("latitude", "37.5665")
                        .param("longitude", "126.9780"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(10))
                .andExpect(jsonPath("$.data.title").value("중고 의자"))
                .andExpect(jsonPath("$.data.isOwner").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/products multipart 요청으로 상품을 등록한다")
    void createProduct_success() throws Exception {
        // 상품 JSON과 이미지 파일 파트가 각각 처리되는지 검증한다.
        String requestJson = """
                {
                  "title":"중고 의자",
                  "businessCategory":"KOREAN_MEAL",
                  "productCategory":"CHAIR_SOFA_BAR_CHAIR",
                  "price":100000,
                  "tradeMethods":["DIRECT"],
                  "tradeLocation":"서울시 중구",
                  "description":"상태가 좋습니다.",
                  "latitude":37.5665,
                  "longitude":126.9780
                }
                """;
        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes()
        );
        MockMultipartFile imagePart = new MockMultipartFile(
                "images", "chair.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3}
        );
        given(productImageService.upload(anyList())).willReturn(List.of("uploaded.jpg"));
        given(productService.createProduct(eq(1L), any(ProductCreateRequest.class),
                eq(List.of("uploaded.jpg"))))
                .willReturn(new ProductCreateResponse(
                        10L, ProductStatus.SELLING, List.of("uploaded.jpg"),
                        LocalDateTime.of(2026, 7, 21, 10, 0)
                ));

        mockMvc.perform(multipart("/api/v1/products")
                        .file(requestPart)
                        .file(imagePart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(10))
                .andExpect(jsonPath("$.data.status").value("SELLING"))
                .andExpect(jsonPath("$.data.imageUrls[0]").value("uploaded.jpg"));

        verify(productImageService).upload(argThat(images -> images.size() == 1));
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} 요청으로 상품을 삭제한다")
    void deleteProduct_success() throws Exception {
        // 상품 삭제 endpoint와 빈 성공 응답을 검증한다.
        mockMvc.perform(delete("/api/v1/products/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(productService).deleteProduct(10L, 1L);
    }

    @Test
    @DisplayName("PATCH /api/v1/products/{id}/status 요청으로 거래 상태를 변경한다")
    void updateProductStatus_success() throws Exception {
        // 상태 변경 JSON body와 응답 상태값을 검증한다.
        given(productService.updateProductStatus(1L, 10L, "SOLD_OUT"))
                .willReturn(new ProductStatusResponse(
                        10L, ProductStatus.SOLD_OUT, LocalDateTime.of(2026, 7, 21, 11, 0)
                ));

        mockMvc.perform(patch("/api/v1/products/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SOLD_OUT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(10))
                .andExpect(jsonPath("$.data.status").value("SOLD_OUT"));
    }

    @Test
    @DisplayName("상품 찜 추가와 취소 API가 찜 상태를 반환한다")
    void bookmarkApis_success() throws Exception {
        // 찜 추가·취소 endpoint가 동일한 응답 구조를 사용하는지 검증한다.
        given(productService.createProductBookmark(1L, 10L))
                .willReturn(new ProductBookmarkResponse(10L, true));
        given(productService.deleteProductBookmark(1L, 10L))
                .willReturn(new ProductBookmarkResponse(10L, false));

        mockMvc.perform(post("/api/v1/products/10/bookmark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isBookmarked").value(true));

        mockMvc.perform(delete("/api/v1/products/10/bookmark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isBookmarked").value(false));
    }

    @Test
    @DisplayName("내 상품과 찜 상품 목록 API가 커서 응답을 반환한다")
    void memberProductLists_success() throws Exception {
        // 사용자 전용 목록 endpoint와 커서 메타데이터를 검증한다.
        ProductListResponse<ProductSummaryResponse, Long> response =
                new ProductListResponse<>(List.of(), CursorPageResponse.of(30L, true));
        given(productService.getMyProducts(eq(1L), any(MyProductListRequest.class)))
                .willReturn(response);
        given(productService.getBookmarkedProducts(eq(1L), any(ProductBookmarkListRequest.class)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/products/me").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page.nextCursor").value(30))
                .andExpect(jsonPath("$.data.page.hasNext").value(true));

        mockMvc.perform(get("/api/v1/products/bookmarks")
                        .param("size", "20")
                        .param("latitude", "37.5665")
                        .param("longitude", "126.9780"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page.nextCursor").value(30));
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} multipart 요청으로 상품을 수정한다")
    void updateProduct_success() throws Exception {
        // 최종 상품 JSON과 새 이미지 파일 파트가 Service로 전달되는지 검증한다.
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setTitle("수정된 의자");
        request.setBusinessCategory(com.closing.closing.domain.product.entity.BusinessCategory.KOREAN_MEAL);
        request.setProductCategory(com.closing.closing.domain.product.entity.ProductCategory.CHAIR_SOFA_BAR_CHAIR);
        request.setPrice(120_000);
        request.setTradeMethods(List.of(com.closing.closing.domain.product.entity.TradeMethod.DIRECT));
        request.setTradeLocation("서울시 종로구");
        request.setLatitude(new BigDecimal("37.5700"));
        request.setLongitude(new BigDecimal("126.9800"));
        request.setDescription("수정된 설명입니다.");
        request.setRetainedImages(List.of("keep.jpg"));
        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
        MockMultipartFile imagePart = new MockMultipartFile(
                "newImages", "new.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1}
        );
        given(productService.updateProduct(eq(1L), eq(10L),
                any(ProductUpdateRequest.class), anyList()))
                .willReturn(new ProductUpdateResponse(
                        10L, "수정된 의자", 120_000,
                        request.getTradeMethods(), "서울시 종로구",
                        request.getLatitude(), request.getLongitude(), ProductStatus.SELLING,
                        List.of("keep.jpg", "new.jpg"), LocalDateTime.of(2026, 7, 21, 12, 0)
                ));

        mockMvc.perform(multipart("/api/v1/products/10")
                        .file(requestPart)
                        .file(imagePart)
                        .with(httpRequest -> {
                            httpRequest.setMethod("PUT");
                            return httpRequest;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 의자"))
                .andExpect(jsonPath("$.data.imageUrls.length()").value(2));
    }
}
