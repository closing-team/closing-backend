package com.closing.closing.domain.support.auth;

import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.closing.closing.global.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportAuthenticationTest {

    private static final Long USER_ID = 1L;
    private static final String TOKEN = "access-token";
    private static final String AUTHORIZATION_HEADER = "Bearer " + TOKEN;

    @Mock
    private JwtProvider jwtProvider;

    private SupportAuthentication supportAuthentication;

    @BeforeEach
    void setUp() {
        supportAuthentication = new SupportAuthentication(jwtProvider);
        lenient().when(jwtProvider.isSignupToken(TOKEN)).thenReturn(false);
        lenient().when(jwtProvider.getUserId(TOKEN)).thenReturn(USER_ID);
    }

    @Test
    @DisplayName("유효한 Authorization 헤더에서 사용자 ID를 추출한다")
    void resolveUserId_Success() {
        Long result = supportAuthentication.resolveUserId(AUTHORIZATION_HEADER);

        assertEquals(USER_ID, result);
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증에 실패한다")
    void resolveUserId_Fail_WhenAuthorizationHeaderMissing() {
        CustomException exception = assertThrows(
                CustomException.class,
                () -> supportAuthentication.resolveUserId(null));

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    @DisplayName("Bearer 형식이 아니면 인증에 실패한다")
    void resolveUserId_Fail_WhenAuthorizationHeaderMalformed() {
        CustomException exception = assertThrows(
                CustomException.class,
                () -> supportAuthentication.resolveUserId(TOKEN));

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    @DisplayName("Bearer 토큰이 비어 있으면 인증에 실패한다")
    void resolveUserId_Fail_WhenTokenBlank() {
        CustomException exception = assertThrows(
                CustomException.class,
                () -> supportAuthentication.resolveUserId("Bearer "));

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 인증에 실패한다")
    void resolveUserId_Fail_WhenTokenInvalid() {
        doThrow(new IllegalArgumentException("유효하지 않은 토큰입니다."))
                .when(jwtProvider)
                .validate("invalid-token");

        CustomException exception = assertThrows(
                CustomException.class,
                () -> supportAuthentication.resolveUserId("Bearer invalid-token"));

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    @DisplayName("가입 토큰이면 인증에 실패한다")
    void resolveUserId_Fail_WhenSignupToken() {
        when(jwtProvider.isSignupToken(TOKEN)).thenReturn(true);

        CustomException exception = assertThrows(
                CustomException.class,
                () -> supportAuthentication.resolveUserId(AUTHORIZATION_HEADER));

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    @DisplayName("토큰에 사용자 ID가 없으면 인증에 실패한다")
    void resolveUserId_Fail_WhenUserIdMissing() {
        when(jwtProvider.getUserId(TOKEN)).thenReturn(null);

        CustomException exception = assertThrows(
                CustomException.class,
                () -> supportAuthentication.resolveUserId(AUTHORIZATION_HEADER));

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }
}
