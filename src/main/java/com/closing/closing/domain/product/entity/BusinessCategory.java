package com.closing.closing.domain.product.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = """
        업종별 카테고리:
        KOREAN_MEAL(한식/백반), CHICKEN_PUB_BBQ(치킨/호프/고깃집),
        CHINESE_HIGH_HEAT_KITCHEN(중식/고화력주방), JAPANESE_WESTERN(일식/양식),
        DESSERT_CAFE_PACKAGE(디저트카페 패키지), TAKEOUT_SPECIALTY(테이크아웃 전문),
        BAKERY_EQUIPMENT(베이커리 전문장비), OFFICE_FURNITURE_SET(사무실 가구 일괄),
        ACADEMY_STUDY_ROOM_EQUIPMENT(학원/독서실 집기),
        HAIR_SALON_BARBERSHOP(미용실/바버샵), NAIL_SKINCARE_SALON(네일/피부 관리실),
        GYM_PILATES(헬스/필라테스), CLOTHING_UNMANNED_STORE(의류 매장/무인 점포)
        """)
@RequiredArgsConstructor
@Getter
public enum BusinessCategory {
    KOREAN_MEAL("한식/백반"),
    CHICKEN_PUB_BBQ("치킨/호프/고깃집"),
    CHINESE_HIGH_HEAT_KITCHEN("중식/고화력주방"),
    JAPANESE_WESTERN("일식/양식"),
    DESSERT_CAFE_PACKAGE("디저트카페 패키지"),
    TAKEOUT_SPECIALTY("테이크아웃 전문"),
    BAKERY_EQUIPMENT("베이커리 전문장비"),
    OFFICE_FURNITURE_SET("사무실 가구 일괄"),
    ACADEMY_STUDY_ROOM_EQUIPMENT("학원/독서실 집기"),
    HAIR_SALON_BARBERSHOP("미용실/바버샵"),
    NAIL_SKINCARE_SALON("네일/피부 관리실"),
    GYM_PILATES("헬스/필라테스"),
    CLOTHING_UNMANNED_STORE("의류 매장/무인 점포");

    private final String displayName;

}
