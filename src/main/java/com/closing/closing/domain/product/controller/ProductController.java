package com.closing.closing.domain.product.controller;

import com.closing.closing.domain.product.dto.request.*;
import com.closing.closing.domain.product.dto.response.*;
import com.closing.closing.domain.product.service.ProductImageService;
import com.closing.closing.domain.product.service.ProductService;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "09. Product", description = "중고거래 상품 API")
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 목록 조회 성공",
                    useReturnTypeSchema = true
            )
    })
    @GetMapping
    public ApiResponse<ProductListResponse<ProductSummaryResponse, String>> getProducts(
            @AuthenticationPrincipal Long userId,
            @ParameterObject
            @Valid @ModelAttribute ProductListRequest request
    ) {
        ProductListResponse<ProductSummaryResponse, String> response =
                productService.getProducts(userId, request);

        return ApiResponse.onSuccess(response);
    }

    // 상품 조회
    @Operation(
            summary = "상품 상세 조회",
            description = "상품 ID에 해당하는 상품의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 상세 조회 성공",
                    useReturnTypeSchema = true
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "상품을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "상품 없음",
                                    value = """
                                        {
                                          "success": false,
                                          "code": "PRODUCT404",
                                          "message": "상품을 찾을 수 없습니다."
                                        }
                                        """
                            )
                    )
            )
    })
    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProduct(
            @AuthenticationPrincipal Long userId,
            @Parameter(
                    description = "조회할 상품 ID",
                    example = "15",
                    required = true
            )
            @PathVariable("productId") Long productId,
            @ParameterObject
            @Valid @ModelAttribute ProductRequest request
    ) {
        ProductResponse response = productService.getProduct(productId, userId, request);

        return ApiResponse.onSuccess(response);
    }

    // 상품 등록
    @Operation(
            summary = "상품 등록",
            description = "상품 정보를 request JSON 파트로, 상품 이미지를 images 파일 파트로 전달합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 등록 성공",
                    useReturnTypeSchema = true
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductCreateResponse> createProduct(
            @AuthenticationPrincipal Long userId,
            @Parameter(
                    description = """
                            상품 등록 정보 JSON입니다. Content-Type은 application/json입니다.<br><br>
                            **title** (필수): 상품 제목 / 예시: 업소용 냉장고<br>
                            **businessCategory** (필수): 업종 카테고리 / 예시: KOREAN_MEAL<br>
                            **productCategory** (필수): 품목 카테고리 / 예시: REFRIGERATOR_FREEZER<br>
                            **price** (필수): 상품 가격 / 예시: 350000<br>
                            **tradeMethods** (필수): 거래 방식 배열 / 예시: [DIRECT, DELIVERY]<br>
                            **tradeLocation**: 직거래 장소 / 예시: 서울특별시 중구 명동<br>
                            **latitude**: 직거래 장소 위도 / 예시: 37.5665<br>
                            **longitude**: 직거래 장소 경도 / 예시: 126.9780<br>
                            **description** (필수): 상품 상세 설명 / 예시: 정상 작동합니다.<br><br>
                            DIRECT 거래 시 tradeLocation, latitude, longitude는 필수입니다.
                            """,
                    required = true,
                    schema = @Schema(implementation = ProductCreateRequest.class)
            )
            @Valid @RequestPart("request") ProductCreateRequest request,
            @Parameter(
                    description = """
                            등록할 상품 이미지 파일 목록입니다.<br><br>
                            예시: refrigerator1.jpg, refrigerator2.jpg<br><br>
                            이미지는 1장 이상 10장 이하로 전달해야 합니다.
                            """,
                    required = true,
                    array = @ArraySchema(
                            schema = @Schema(
                                    type = "string",
                                    format = "binary"
                            )
                    )
            )
            @RequestPart("images") List<MultipartFile> images
    ) {

        List<String> imageUrls = productImageService.upload(images);

        ProductCreateResponse response = productService.createProduct(userId, request, imageUrls);

        return ApiResponse.onSuccess(response);
    }

    // 상품 삭제
    @Operation(
            summary = "상품 삭제",
            description = "상품 id를 이용해 상품 한 개를 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 삭제 성공",
                    useReturnTypeSchema = true
            )
    })
    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteProduct(
            @AuthenticationPrincipal Long userId,
            @Parameter(
                    description = "삭제할 상품 ID",
                    example = "15",
                    required = true
            )
            @PathVariable Long productId
    ) {

        productService.deleteProduct(productId, userId);

        return ApiResponse.onSuccess(null);
    }

    // 상품 상태 수정
    @Operation(
            summary = "상품 상태 수정",
            description = "상품 id를 이용해 상품 한 개의 상태를 수정합니다. (SELLING, RESERVED, SOLD_OUT)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 상태 수정 성공",
                    useReturnTypeSchema = true
            )
    })
    @PatchMapping("/{productId}/status")
    public ApiResponse<ProductStatusResponse> updateProductStatus(
            @AuthenticationPrincipal Long userId,
            @Parameter(
                    description = "상태 수정할 상품 ID",
                    example = "15",
                    required = true
            )
            @PathVariable("productId") Long productId,
            @Valid @RequestBody ProductStatusRequest request
    ) {

        ProductStatusResponse response = productService.updateProductStatus(userId, productId, request.getStatus());

        return ApiResponse.onSuccess(response);
    }

    // 상품 찜 추가
    @Operation(
            summary = "상품 북마크 추가",
            description = "상품 id를 이용해 상품을 북마크 목록에 추가합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 북마크 추가 성공",
                    useReturnTypeSchema = true
            )
    })
    @PostMapping("/{productId}/bookmark")
    public ApiResponse<ProductBookmarkResponse> createBookmark(
            @AuthenticationPrincipal Long userId,
            @Parameter(
                    description = "북마크 추가할 상품 ID",
                    example = "15",
                    required = true
            )
            @PathVariable("productId") Long productId
    ) {

        ProductBookmarkResponse response = productService.createProductBookmark(userId, productId);

        return ApiResponse.onSuccess(response);
    }

    // 상품 찜 삭제
    @Operation(
            summary = "상품 북마크 삭제",
            description = "상품 id를 이용해 상품을 북마크 목록에서 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 북마크 삭제 성공",
                    useReturnTypeSchema = true
            )
    })
    @DeleteMapping("/{productId}/bookmark")
    public ApiResponse<ProductBookmarkResponse> deleteBookmark(
            @AuthenticationPrincipal Long userId,
            @Parameter(
                    description = "북마크 삭제할 상품 ID",
                    example = "15",
                    required = true
            )
            @PathVariable("productId") Long productId
    ) {

        ProductBookmarkResponse response = productService.deleteProductBookmark(userId, productId);

        return ApiResponse.onSuccess(response);
    }

    // 내 상품 조회
    @Operation(
            summary = "내 상품 조회",
            description = "현재 사용자가 등록한 상품 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 상품 목록 조회 성공",
                    useReturnTypeSchema = true
            )
    })
    @GetMapping("/me")
    public ApiResponse<MyProductListResponse> getMyProducts(
            @AuthenticationPrincipal Long userId,
            @ParameterObject
            @Valid @ModelAttribute MyProductListRequest request
    ) {
        MyProductListResponse response =
                productService.getMyProducts(userId, request);

        return ApiResponse.onSuccess(response);
    }

    // 찜 상품 조회
    @Operation(
            summary = "북마크 상품 조회",
            description = "현재 사용자가 북마크한 상품 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "북마크 상품 목록 조회 성공",
                    useReturnTypeSchema = true
            )
    })
    @GetMapping("/bookmarks")
    public ApiResponse<ProductListResponse<ProductSummaryResponse, Long>> getBookmarks(
            @AuthenticationPrincipal Long userId,
            @ParameterObject
            @Valid @ModelAttribute ProductBookmarkListRequest request
    ) {
        ProductListResponse<ProductSummaryResponse, Long> response =
                productService.getBookmarkedProducts(userId, request);

        return ApiResponse.onSuccess(response);
    }

    // 상품 수정
    @Operation(
            summary = "상품 수정",
            description = "상품 정보를 request JSON 파트로, 새 이미지를 newImages 파일 파트로 전달합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 수정 성공",
                    useReturnTypeSchema = true
            )
    })
    @PutMapping(value = "/{productId}",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductUpdateResponse> updateProduct(
            @AuthenticationPrincipal Long userId,
            @Parameter(
                    description = "수정할 상품 ID",
                    example = "15",
                    required = true
            )
            @PathVariable Long productId,
            @Parameter(
                    description = """
                            상품 수정 정보 JSON입니다. Content-Type은 application/json입니다.<br><br>
                            **title** (필수): 수정 후 상품 제목 / 예시: 업소용 냉장고<br>
                            **businessCategory** (필수): 업종 카테고리 / 예시: KOREAN_MEAL<br>
                            **productCategory** (필수): 품목 카테고리 / 예시: REFRIGERATOR_FREEZER<br>
                            **price** (필수): 수정 후 가격 / 예시: 300000<br>
                            **tradeMethods** (필수): 거래 방식 배열 / 예시: [DIRECT, DELIVERY]<br>
                            **tradeLocation**: 직거래 장소 / 예시: 서울특별시 중구 명동<br>
                            **latitude**: 직거래 장소 위도 / 예시: 37.5665<br>
                            **longitude**: 직거래 장소 경도 / 예시: 126.9780<br>
                            **description** (필수): 상품 상세 설명 / 예시: 정상 작동합니다.<br>
                            **retainedImages** (필수): 유지할 기존 이미지 URL 배열 / 예시: [https://example.com/image1.jpg]<br><br>
                            DIRECT 거래 시 tradeLocation, latitude, longitude는 필수입니다.
                            """,
                    required = true,
                    schema = @Schema(implementation = ProductUpdateRequest.class)
            )
            @Valid @RequestPart("request") ProductUpdateRequest request,
            @Parameter(
                    description = """
                            새로 추가할 이미지 파일 목록입니다.<br><br>
                            예시: refrigerator1.jpg, refrigerator2.jpg<br><br>
                            새 이미지가 없으면 생략합니다.<br>
                            retainedImages와 합친 최종 이미지 개수는 1장 이상 10장 이하여야 합니다.
                            """,
                    required = false,
                    array = @ArraySchema(
                            schema = @Schema(
                                    type = "string",
                                    format = "binary"
                            )
                    )
            )
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages
    ) {
        ProductUpdateResponse response =
                productService.updateProduct(userId, productId, request, newImages);

        return ApiResponse.onSuccess(response);
    }
}
