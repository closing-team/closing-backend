package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.product.entity.ProductStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChatRoomOtherMemberResponse {

    private final Long memberId;
    private final String nickname;
    private final String profileImageUrl;

}
