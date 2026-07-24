package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SellerResponse {

    private final Long memberId;
    private final String nickname;
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
