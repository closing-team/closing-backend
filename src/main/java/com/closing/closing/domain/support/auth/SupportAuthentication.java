package com.closing.closing.domain.support.auth;

import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.closing.closing.global.jwt.JwtProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SupportAuthentication {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    public Long resolveUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());
        if (token.isBlank()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        try {
            jwtProvider.validate(token);
            if (jwtProvider.isSignupToken(token)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }

            Long userId = jwtProvider.getUserId(token);
            if (userId == null) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
            return userId;
        } catch (IllegalArgumentException | JwtException exception) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }
}
