package com.closing.closing.domain.product.controller;

import com.closing.closing.domain.product.dto.request.ProductCreateRequest;
import com.closing.closing.domain.product.dto.response.ProductCreateResponse;
import com.closing.closing.domain.product.dto.response.ProductResponse;
import com.closing.closing.domain.product.service.ProductImageService;
import com.closing.closing.domain.product.service.ProductService;
import com.closing.closing.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductImageService productImageService;

    // 상품 조회
    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProduct(
            @PathVariable("productId") Long productId
    ) {
        // TODO: userId 하드코딩 X
        // 인증 연결 이전이므로 임시 userId 설정
        // 인증 연결 이후엔 Token으로 user 판별
        Long userId = 1L;

        ProductResponse response = productService.getProduct(productId, userId);

        return ApiResponse.onSuccess(response);
    }

    // 상품 등록
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
    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteProduct(
            @PathVariable Long productId
    ) {
        // TODO: 인증 연결 후 인증 객체에서 추출
        Long userId = 1L;

        productService.deleteProduct(productId, userId);

        return ApiResponse.onSuccess(null);
    }
}
