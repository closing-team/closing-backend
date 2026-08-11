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

    @Schema(
            description = "판매자 활동 지역. 아직 설정하지 않은 경우 null입니다.",
            example = "원흥동",
            nullable = true
    )
    private final String location;

    public static SellerResponse from(User user) {
        return new SellerResponse(
                user.getId(),
                user.getNickname(),
                user.getLocation()
        );
    }
}
