package com.closing.closing.domain.product.controller;

import com.closing.closing.domain.product.dto.ProductResponse;
import com.closing.closing.domain.product.service.ProductService;
import com.closing.closing.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProduct(
            @PathVariable("productId") Long productId
    ) {
        ProductResponse response = productService.getProduct(productId);

        return ApiResponse.onSuccess(response);
    }
}
