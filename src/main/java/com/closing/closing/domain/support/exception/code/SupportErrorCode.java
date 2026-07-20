package com.closing.closing.domain.support.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SupportErrorCode {
    SUPPORT_INVALID_QUERY(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    SUPPORT_API_KEY_NOT_FOUND(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "SUPPORT500",
            "기업마당 API 키가 설정되지 않았습니다."),
    SUPPORT_EXTERNAL_API_ERROR(
            HttpStatus.BAD_GATEWAY,
            "SUPPORT502",
            "기업마당 API 호출에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
