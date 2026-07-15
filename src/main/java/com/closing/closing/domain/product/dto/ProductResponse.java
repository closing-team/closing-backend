package com.closing.closing.domain.product.dto;

import com.closing.closing.domain.product.entity.Product;
import lombok.Getter;

@Getter
public class ProductResponse {

    private final Long id;
    private final String title;
    private final int price;

    public ProductResponse(Long id, String title, int price) {
        this.id = id;
        this.title = title;
        this.price = price;
    }

    // Product 객체로부터 ProductResponse를 만드는 함수
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getTitle(),
                product.getPrice()
        );
    }


}
