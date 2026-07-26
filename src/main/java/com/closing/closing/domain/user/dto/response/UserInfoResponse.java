package com.closing.closing.domain.user.dto.response;

import com.closing.closing.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {

    private Long userId;
    private String name;
    private String nickname;
    private String phone;
    private String email;
    private String profileImageUrl;

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}
