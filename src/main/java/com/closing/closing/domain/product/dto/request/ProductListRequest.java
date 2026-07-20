package com.closing.closing.domain.product.dto.request;

import com.closing.closing.domain.product.entity.BusinessCategory;
import com.closing.closing.domain.product.entity.ProductCategory;
import com.closing.closing.domain.product.entity.SortMethod;
import com.closing.closing.domain.product.entity.TradeMethod;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductListRequest {

    // 검색어가 없으면 null
    private String keyword;

    // 선택하지 않으면 null
    private BusinessCategory businessCategory;

    // 선택하지 않으면 null
    private ProductCategory productCategory;

    // 전달하지 않으면 null - 모두 조회
    // 여러 개 전달 가능
    private List<TradeMethod> tradeMethods;

    // 생략하면 false
    // 근처는 우선 2km로 설정
    private boolean nearby = false;

    // 생략하면 최신순
    private SortMethod sort = SortMethod.LATEST;

    // 정렬 방식마다 cursor가 달라질 수 있으므로 String 타입 cursor
    private String cursor;

    @Min(value = 1, message = "조회 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "조회 크기는 100 이하여야 합니다.")
    private Integer size = 20;

    @NotNull(message = "현재 위치의 위도는 필수입니다.")
    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
    private BigDecimal latitude;

    @NotNull(message = "현재 위치의 경도는 필수입니다.")
    @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
    private BigDecimal longitude;
}
