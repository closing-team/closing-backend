package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "상품 판매자 정보")
@RequiredArgsConstructor
@Getter
public class SellerResponse {

    @Schema(description = "판매자 회원 ID", example = "7")
    private final Long memberId;

    @Schema(description = "판매자 닉네임", example = "마감왕")
    private final String nickname;

    @Schema(description = "판매자 활동 지역. 현재 User 도메인 연동 전이므로 null입니다.", example = "서울특별시 중구")
    private final String location;

    // TODO: User 엔티티에 location 필드 추가
    // 현재 User에 location 필드가 없기 때문에 null 반환
    public static SellerResponse from(User user) {
        return new SellerResponse(
                user.getId(),
                user.getNickname(),
                null
                // user.getLocation()
        );
    }
}
