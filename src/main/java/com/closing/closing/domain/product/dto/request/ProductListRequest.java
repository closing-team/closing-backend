package com.closing.closing.domain.product.dto.request;

import com.closing.closing.domain.product.entity.BusinessCategory;
import com.closing.closing.domain.product.entity.ProductCategory;
import com.closing.closing.domain.product.entity.SortMethod;
import com.closing.closing.domain.product.entity.TradeMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "상품 목록 검색·필터·정렬 및 커서 페이징 조건")
public class ProductListRequest {

    // 검색어가 없으면 null
    @Schema(
            description = "상품명 검색어. 검색하지 않을 경우 생략합니다.",
            example = "업소용 냉장고"
    )
    private String keyword;

    // 선택하지 않으면 null
    @Schema(
            description = "업종별 카테고리. 선택하지 않을 경우 생략합니다.",
            example = "KOREAN_MEAL"
    )
    private BusinessCategory businessCategory;

    // 선택하지 않으면 null
    @Schema(
            description = "품목별 카테고리. 선택하지 않을 경우 생략합니다.",
            example = "REFRIGERATOR_FREEZER"
    )
    private ProductCategory productCategory;

    // 전달하지 않으면 null - 모두 조회
    // 여러 개 전달 가능
    @Schema(
            description = "거래 방식 목록. 생략하면 거래 방식과 관계 없이 조회합니다. 여러 값을 전달하면 해당 거래 방식을 모두 지원하는 상품을 조회합니다."
    )
    private List<TradeMethod> tradeMethods;

    // 생략하면 false
    // 근처는 우선 2km로 설정
    @Schema(
            description = "현재 위치로부터 2km 이내 상품만 조회할지 여부",
            example = "false",
            defaultValue = "false"
    )
    private boolean nearby = false;

    // 생략하면 최신순
    @Schema(
            description = "상품 정렬 방식",
            example = "LATEST",
            defaultValue = "LATEST"
    )
    private SortMethod sort = SortMethod.LATEST;

    // 정렬 방식마다 cursor가 달라질 수 있으므로 String 타입 cursor
    @Schema(
            description = """
                    다음 페이지 조회용 커서입니다. 첫 요청에서는 생략하고,
                    이후 요청부터 직전 응답의 nextCursor 값을 그대로 전달합니다.
                    정렬별 형식은 LATEST=상품ID,
                    CHEAPEST/MOST_EXPENSIVE=가격:상품ID,
                    POPULAR=북마크수:상품ID,
                    NEAREST=거리(km):상품ID입니다.
                    """,
            example = "10000:15"
    )
    private String cursor;

    @Schema(
            description = "한 번에 조회할 상품 개수",
            example = "20",
            defaultValue = "20",
            minimum = "1",
            maximum = "100"
    )
    @Min(value = 1, message = "조회 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "조회 크기는 100 이하여야 합니다.")
    @NotNull
    private Integer size = 20;

    @Schema(
            description = "사용자 현재 위치의 위도",
            example = "37.5665",
            minimum = "-90",
            maximum = "90",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "현재 위치의 위도는 필수입니다.")
    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
    private BigDecimal latitude;

    @Schema(
            description = "사용자 현재 위치의 경도",
            example = "126.9780",
            minimum = "-180",
            maximum = "180",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "현재 위치의 경도는 필수입니다.")
    @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
    private BigDecimal longitude;
}
