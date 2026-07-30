package com.closing.closing.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private final boolean isNewUser;
    private final String accessToken;
    private final String refreshToken;

    public static LoginResponse of(String accessToken, String refreshToken, boolean isNewUser) {
        return LoginResponse.builder()
                .isNewUser(isNewUser)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
