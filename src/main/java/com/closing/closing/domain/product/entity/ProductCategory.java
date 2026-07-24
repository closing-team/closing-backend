package com.closing.closing.domain.product.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = """
        품목별 카테고리:
        REFRIGERATOR_FREEZER(냉장고/냉동고), DISHWASHER_FRYER(식기세척기/튀김기),
        SINK_WORKTABLE(싱크대/작업대), ICE_MAKER_SHOWCASE(제빙기/쇼케이스),
        COMMERCIAL_TABLE(업소용 테이블), LIGHTING_STORE_CABINET(조명/매장 수납장),
        CHAIR_SOFA_BAR_CHAIR(의자/소파/바 체어), OFFICE_DESK_CHAIR(사무용 책상/의자),
        DISHWARE_TABLEWARE_CONSUMABLES(그릇/식기/소모품),
        HEATING_COOLING_APPLIANCE(냉난방기/가전),
        POS_KIOSK_BILL_PRINTER(POS/키오스크/빌지프린터)
        """)
@RequiredArgsConstructor
@Getter
public enum ProductCategory {
    REFRIGERATOR_FREEZER("냉장고/냉동고"),
    DISHWASHER_FRYER("식기세척기/튀김기"),
    SINK_WORKTABLE("싱크대/작업대"),
    ICE_MAKER_SHOWCASE("제빙기/쇼케이스"),
    COMMERCIAL_TABLE("업소용 테이블"),
    LIGHTING_STORE_CABINET("조명/매장 수납장"),
    CHAIR_SOFA_BAR_CHAIR("의자/소파/바 체어"),
    OFFICE_DESK_CHAIR("사무용 책상/의자"),
    DISHWARE_TABLEWARE_CONSUMABLES("그릇/식기/소모품"),
    HEATING_COOLING_APPLIANCE("냉난방기/가전"),
    POS_KIOSK_BILL_PRINTER("POS/키오스크/빌지프린터");

    private final String displayName;
}
