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
    IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_DELETE_FAILED", "이미지 삭제에 실패했습니다.");
    //INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_IMAGE_FORMAT", "지원하지 않는 이미지 형식입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

