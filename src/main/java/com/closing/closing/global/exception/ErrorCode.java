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
    // 중고거래 에러코드
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT404", "상품을 찾을 수 없습니다."),
    TRADE_LOCATION_REQUIRED(HttpStatus.BAD_REQUEST, "TRADE_LOCATION_REQUIRED", "직거래 장소를 입력해야 합니다."),
    INVALID_IMAGE_COUNT(HttpStatus.BAD_REQUEST, "INVALID_IMAGE_COUNT", "상품 이미지는 1장 이상 10장 이하로 등록해야 합니다."),
    PRODUCT_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "PRODUCT_DELETE_FORBIDDEN", "본인의 상품만 삭제할 수 있습니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_UPLOAD_FAILED", "이미지 업로드에 실패했습니다."),
    IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_DELETE_FAILED", "이미지 삭제에 실패했습니다."),
    PRODUCT_STATUS_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "PRODUCT_STATUS_UPDATE_FORBIDDEN", "본인의 상품만 상태를 수정할 수 있습니다."),
    INVALID_PRODUCT_STATUS(HttpStatus.BAD_REQUEST, "INVALID_PRODUCT_STATUS", "지원하지 않는 상품 상태입니다."),
    PRODUCT_BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_BOOKMARK_NOT_FOUND", "찜 상품을 찾을 수 없습니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "INVALID_CURSOR", "올바르지 않은 커서입니다."),
    PRODUCT_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "PRODUCT_UPDATE_FORBIDDEN", "본인의 상품만 수정할 수 있습니다."),
    INVALID_RETAINED_IMAGES(HttpStatus.BAD_REQUEST, "INVALID_RETAINED_IMAGES", "올바르지 않은 기존 이미지입니다."),
    //INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_IMAGE_FORMAT", "지원하지 않는 이미지 형식입니다.");

    AI_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AI401", "인증 토큰이 없거나 만료되었습니다."),
    AI_EMPTY_INITIAL_INPUT(HttpStatus.BAD_REQUEST, "AI_INITIAL_INPUT400", "초기 상황 입력 내용이 없습니다."),
    AI_LLM_GENERATION_FAILED(HttpStatus.FAILED_DEPENDENCY, "AI424", "일정 생성에 실패했습니다."),
    AI_LLM_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "AI504", "AI 응답 시간이 초과되었습니다."),
    AI_RAG_SEARCH_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "AI503", "참고 문서 검색에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

