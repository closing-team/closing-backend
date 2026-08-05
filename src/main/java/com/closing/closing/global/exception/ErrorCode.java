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
    USER_PHONE_INVALID(HttpStatus.BAD_REQUEST, "USER_PHONE_INVALID400", "전화번호 형식이 올바르지 않습니다."),

    TERM_REQUIRED(HttpStatus.BAD_REQUEST, "TERM_REQUIRED400", "필수 약관에 모두 동의해야 합니다."),

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

    // 채팅 에러코드
    SELF_CHAT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "SELF_CHAT_NOT_ALLOWED", "본인 상품에 문의할 수 없습니다."),
    CHAT_PRODUCT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "CHAT_PRODUCT_NOT_AVAILABLE", "판매 완료된 상품에 문의할 수 없습니다."),
    EMPTY_CHAT_MESSAGE(HttpStatus.BAD_REQUEST, "EMPTY_CHAT_MESSAGE", "메시지 내용 또는 이미지를 전달해야 합니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "CHAT_ROOM_ACCESS_FORBIDDEN", "해당 채팅방에 참여할 수 없습니다."),
    MULTIPLE_CHAT_MESSAGE_TYPES(HttpStatus.BAD_REQUEST, "MULTIPLE_CHAT_MESSAGE_TYPES", "텍스트와 이미지는 동시에 전송할 수 없습니다."),
    //INVALID_CHAT_IMAGE_COUNT(HttpStatus.BAD_REQUEST, "INVALID_CHAT_IMAGE_COUNT", "채팅 이미지는 1장 이상 10장 이하로 전송해야 합니다."),
    INVALID_CHAT_IMAGE(HttpStatus.BAD_REQUEST, "INVALID_CHAT_IMAGE", "잘못된 이미지 형식입니다."),

    // 일정 에러코드
    TASK_TITLE_BLANK(HttpStatus.BAD_REQUEST, "TASK400", "일정 제목이 비어있습니다."),
    TASK_INVALID_PERIOD(HttpStatus.BAD_REQUEST, "TASK_PERIOD400", "일정 기간이 올바르지 않습니다."),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "TASK404", "일정을 찾을 수 없습니다."),

    // 지원정보 에러코드
    SUPPORT_INVALID_QUERY(
            HttpStatus.BAD_REQUEST,
            "SUPPORT400",
            "지원정보 조회 조건이 올바르지 않습니다."),
    BOOKMARK_INVALID_QUERY(
            HttpStatus.BAD_REQUEST,
            "BOOKMARK400",
            "북마크 요청값이 올바르지 않습니다."),
    SUPPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "SUPPORT404", "존재하지 않는 지원정보입니다."),
    BOOKMARK_ALREADY_EXISTS(HttpStatus.CONFLICT, "BOOKMARK409", "이미 등록된 북마크입니다."),
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOKMARK404", "북마크를 찾을 수 없습니다."),
    SUPPORT_API_KEY_NOT_FOUND(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "SUPPORT500",
            "기업마당 API 키가 설정되지 않았습니다."),
    SUPPORT_EXTERNAL_API_ERROR(
            HttpStatus.BAD_GATEWAY,
            "SUPPORT502",
            "기업마당 API 호출에 실패했습니다."),

    // AI세션 에러코드
    AI_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AI401", "인증 토큰이 없거나 만료되었습니다."),
    AI_EMPTY_INITIAL_INPUT(HttpStatus.BAD_REQUEST, "AI_INITIAL_INPUT400", "초기 상황 입력 내용이 없습니다."),
    AI_LLM_GENERATION_FAILED(HttpStatus.FAILED_DEPENDENCY, "AI424", "일정 생성에 실패했습니다."),
    AI_LLM_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "AI504", "AI 응답 시간이 초과되었습니다."),
    AI_RAG_SEARCH_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "AI503", "참고 문서 검색에 실패했습니다."),
    AI_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_SESSION404", "존재하지 않는 세션입니다."),
    AI_SESSION_ALREADY_CONFIRMED(HttpStatus.CONFLICT, "AI_SESSION409", "이미 확정되어 더 이상 진행할 수 없는 세션입니다."),
    AI_EMPTY_MESSAGE(HttpStatus.BAD_REQUEST, "AI_MESSAGE400", "대화 메시지 내용이 없습니다."),
    AI_SESSION_TASKS_GENERATED(HttpStatus.CONFLICT, "AI_SESSION_TASKS409", "이미 일정이 생성되어 더 이상 대화를 진행할 수 없는 세션입니다."),
    AI_SESSION_CONCURRENT_UPDATE(
            HttpStatus.CONFLICT, "AI_SESSION_CONCURRENT409", "다른 요청이 동시에 처리되고 있어 다시 시도해야 합니다."),
    AI_TEMP_TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_TEMP_TASK404", "존재하지 않는 임시 일정입니다."),
    AI_EMPTY_TASK_TITLE(HttpStatus.BAD_REQUEST, "AI_TASK_TITLE400", "일정 제목이 비어있습니다."),
    AI_SESSION_ACCESS_FORBIDDEN(
            HttpStatus.FORBIDDEN, "AI_SESSION_ACCESS_FORBIDDEN", "본인의 세션만 접근할 수 있습니다."),
    AI_NO_TASKS_TO_CONFIRM(HttpStatus.CONFLICT, "AI_NO_TASKS409", "확정할 일정이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
