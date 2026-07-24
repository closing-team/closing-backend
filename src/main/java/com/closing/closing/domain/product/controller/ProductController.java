package com.closing.closing.domain.product.controller;

import com.closing.closing.domain.product.dto.request.*;
import com.closing.closing.domain.product.dto.response.*;
import com.closing.closing.domain.product.service.ProductImageService;
import com.closing.closing.domain.product.service.ProductService;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Product", description = "중고거래 상품 API")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductImageService productImageService;

    // 상품 다건 조회
    @Operation(
            summary = "상품 목록 조회",
            description = "검색어, 카테고리, 거래 방식, 정렬 조건을 적용하여 상품 목록을 커서 기반으로 조회합니다."
    )
    @GetMapping
    public ApiResponse<ProductListResponse<ProductSummaryResponse, String>> getProducts(
            @Valid @ModelAttribute ProductListRequest request
    ) {
        // TODO: 인증 연결 후 인증 객체에서 추출
        Long userId = 1L;

        ProductListResponse<ProductSummaryResponse, String> response =
                productService.getProducts(userId, request);

        return ApiResponse.onSuccess(response);
    }

    // 상품 조회
    @Operation(
            summary = "상품 상세 조회",
            description = "상품 ID에 해당하는 상품의 상세 정보를 조회합니다."
    )
    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProduct(
            @PathVariable("productId") Long productId,
            @Valid @ModelAttribute ProductRequest request
    ) {
        // TODO: userId 하드코딩 X
        // 인증 연결 이전이므로 임시 userId 설정
        // 인증 연결 이후엔 Token으로 user 판별
        Long userId = 1L;

        ProductResponse response = productService.getProduct(productId, userId, request);

        return ApiResponse.onSuccess(response);
    }

    // 상품 등록
    @Operation(
            summary = "상품 등록",
            description = "상품 등록에 필요한 정보를 바탕으로 상품을 등록합니다."
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductCreateResponse> createProduct(
            @Valid @RequestPart("request") ProductCreateRequest request,
            @RequestPart("images") List<MultipartFile> images
    ) {
        // TODO: 인증 연결 후 인증 객체에서 추출
        Long userId = 1L;

        List<String> imageUrls = productImageService.upload(images);

        ProductCreateResponse response = productService.createProduct(userId, request, imageUrls);

        return ApiResponse.onSuccess(response);
    }

    // 상품 삭제
    @Operation(
            summary = "상품 삭제",
            description = "상품 id를 이용해 상품 한 개를 삭제합니다."
    )
    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteProduct(
            @PathVariable Long productId
    ) {
        // TODO: 인증 연결 후 인증 객체에서 추출
        Long userId = 1L;

        productService.deleteProduct(productId, userId);

        return ApiResponse.onSuccess(null);
    }

    // 상품 상태 수정
    @Operation(
            summary = "상품 상태 수정",
            description = "상품 id를 이용해 상품 한 개의 상태를 수정합니다. (SELLING, RESERVED, SOLD_OUT)"
    )
    @PatchMapping("/{productId}/status")
    public ApiResponse<ProductStatusResponse> updateProductStatus(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody ProductStatusRequest request
    ) {
        // TODO: 인증 연결 후 인증 객체에서 추출
        Long userId = 1L;

        ProductStatusResponse response = productService.updateProductStatus(userId, productId, request.getStatus());

        return ApiResponse.onSuccess(response);
    }

    // 상품 찜 추가
    @Operation(
            summary = "상품 북마크 추가",
            description = "상품 id를 이용해 상품을 북마크 목록에 추가합니다."
    )
    @PostMapping("/{productId}/bookmark")
    public ApiResponse<ProductBookmarkResponse> createBookmark(
            @PathVariable("productId") Long productId
    ) {
        // TODO: 인증 연결 후 인증 객체에서 추출
        Long userId = 1L;

        ProductBookmarkResponse response = productService.createProductBookmark(userId, productId);

        return ApiResponse.onSuccess(response);
    }

    // 상품 찜 삭제
    @Operation(
            summary = "상품 북마크 삭제",
            description = "상품 id를 이용해 상품을 북마크 목록에서 삭제합니다."
    )
    @DeleteMapping("/{productId}/bookmark")
    public ApiResponse<ProductBookmarkResponse> deleteBookmark(
            @PathVariable("productId") Long productId
    ) {
        // TODO: 인증 객체에서 추출
        Long userId = 1L;

        ProductBookmarkResponse response = productService.deleteProductBookmark(userId, productId);

        return ApiResponse.onSuccess(response);
    }

    // 내 상품 조회
    @Operation(
            summary = "내 상품 조회",
            description = "현재 사용자가 등록한 상품 목록을 조회합니다."
    )
    @GetMapping("/me")
    public ApiResponse<MyProductListResponse> getMyProducts(
            @Valid @ModelAttribute MyProductListRequest request
    ) {
        // TODO: 인증
        Long userId = 1L;
        MyProductListResponse response =
                productService.getMyProducts(userId, request);

        return ApiResponse.onSuccess(response);
    }

    // 찜 상품 조회
    @Operation(
            summary = "북마크 상품 조회",
            description = "현재 사용자가 북마크한 상품 목록을 조회합니다."
    )
    @GetMapping("/bookmarks")
    public ApiResponse<ProductListResponse<ProductSummaryResponse, Long>> getBookmarks(
            @Valid @ModelAttribute ProductBookmarkListRequest request
    ) {
        // TODO: 인증
        Long userId = 1L;
        ProductListResponse<ProductSummaryResponse, Long> response =
                productService.getBookmarkedProducts(userId, request);

        return ApiResponse.onSuccess(response);
    }

    // 상품 수정
    @Operation(
            summary = "상품 수정",
            description = "상품 ID에 해당하는 상품 정보와 이미지를 수정합니다."
    )
    @PutMapping(value = "/{productId}",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductUpdateResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestPart("request") ProductUpdateRequest request,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages
    ) {
        Long userId = 1L;
        ProductUpdateResponse response =
                productService.updateProduct(userId, productId, request, newImages);

        return ApiResponse.onSuccess(response);
    }
}
