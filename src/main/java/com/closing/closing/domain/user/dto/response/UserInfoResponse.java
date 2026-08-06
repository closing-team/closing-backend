package com.closing.closing.domain.user.dto.response;

import com.closing.closing.domain.business.entity.BusinessRegistration;
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
    private String businessNumber;
    private boolean isBusinessVerified;

    public static UserInfoResponse from(User user, BusinessRegistration business) {
        return UserInfoResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .businessNumber(business != null ? formatBusinessNumber(business.getBusinessNumber()) : null)
                .isBusinessVerified(business != null)
                .build();
    }

    private static String formatBusinessNumber(String number) {
        if (number == null || number.length() != 10) return number;
        return number.substring(0, 3) + "-" + number.substring(3, 5) + "-" + number.substring(5);
    }
}
