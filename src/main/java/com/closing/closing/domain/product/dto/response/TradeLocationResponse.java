package com.closing.closing.domain.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "상품 직거래 위치 정보")
@Getter
@RequiredArgsConstructor
public class TradeLocationResponse {

    @Schema(description = "직거래 장소의 주소 또는 지역명", example = "서울특별시 중구 명동")
    private final String district;

    @Schema(
            description = "사용자 현재 위치와 직거래 장소 사이의 거리(km). 현재 위치를 전달하지 않은 경우 null입니다.",
            example = "1.25"
    )
    private final Double distanceKm;

    public static TradeLocationResponse of(
            String district,
            Double distanceKm
    ) {
        return new TradeLocationResponse(
                district,
                distanceKm
        );
    }
}
