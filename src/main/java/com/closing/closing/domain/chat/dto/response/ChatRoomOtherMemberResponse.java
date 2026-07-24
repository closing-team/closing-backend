package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.chat.entity.ChatRoom;
import com.closing.closing.domain.product.entity.ProductStatus;
import com.closing.closing.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChatRoomOtherMemberResponse {

    private final Long memberId;
    private final String nickname;
    private final String profileImageUrl;

    public static ChatRoomOtherMemberResponse from(User user) {
        return new ChatRoomOtherMemberResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }

}
