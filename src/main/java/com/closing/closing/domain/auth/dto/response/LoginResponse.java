package com.closing.closing.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private final boolean isNewUser;

    // 기존 유저
    private final String accessToken;
    private final String refreshToken;

    // 신규 유저 (회원가입 플로우 진입)
    private final String signupToken;

    public static LoginResponse ofExistingUser(String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .isNewUser(false)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public static LoginResponse ofNewUser(String signupToken) {
        return LoginResponse.builder()
                .isNewUser(true)
                .signupToken(signupToken)
                .build();
    }
}
