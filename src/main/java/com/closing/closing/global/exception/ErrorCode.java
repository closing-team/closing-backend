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

    AI_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AI401", "인증 토큰이 없거나 만료되었습니다."),
    AI_EMPTY_INITIAL_INPUT(HttpStatus.BAD_REQUEST, "AI_INITIAL_INPUT400", "초기 상황 입력 내용이 없습니다."),
    AI_LLM_GENERATION_FAILED(HttpStatus.FAILED_DEPENDENCY, "AI424", "일정 생성에 실패했습니다."),
    AI_LLM_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "AI504", "AI 응답 시간이 초과되었습니다."),
    AI_RAG_SEARCH_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "AI503", "참고 문서 검색에 실패했습니다."),
    AI_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_SESSION404", "존재하지 않는 세션입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}