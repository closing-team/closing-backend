package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "채팅 상대방 정보")
@Getter
@RequiredArgsConstructor
public class ChatRoomOtherMemberResponse {

    @Schema(description = "상대방 회원 ID", example = "7")
    private final Long memberId;

    @Schema(description = "상대방 닉네임", example = "마감왕")
    private final String nickname;

    @Schema(description = "상대방 프로필 이미지 URL. 등록된 이미지가 없으면 null입니다.", example = "https://example.com/profiles/7.jpg")
    private final String profileImageUrl;

    public static ChatRoomOtherMemberResponse from(User user) {
        return new ChatRoomOtherMemberResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }

}
