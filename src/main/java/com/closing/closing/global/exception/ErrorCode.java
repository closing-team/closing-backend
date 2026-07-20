package com.closing.closing.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),

    AUTH_KAKAO(HttpStatus.UNAUTHORIZED, "AUTH_KAKAO401", "카카오 인증에 실패했습니다."),
    AUTH_KAKAO_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_KAKAO_EXPIRED401", "카카오 토큰이 만료되었습니다."),
    AUTH_SIGNUP_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_SIGNUP_TOKEN404", "유효하지 않거나 만료된 가입 토큰입니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404", "사용자를 찾을 수 없습니다."),
    USER_PHONE_INVALID(HttpStatus.BAD_REQUEST, "USER_PHONE_INVALID400", "전화번호 형식이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}