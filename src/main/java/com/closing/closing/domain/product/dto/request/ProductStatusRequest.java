package com.closing.closing.domain.product.dto.request;

import com.closing.closing.domain.product.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ProductStatusRequest {

    @NotBlank(message = "상품 상태는 필수입니다.")
    private String status;

}
