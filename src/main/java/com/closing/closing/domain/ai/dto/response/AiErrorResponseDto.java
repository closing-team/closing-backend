package com.closing.closing.domain.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 서버 오류 응답 (내부 연동용)")
public record AiErrorResponseDto(
        @Schema(description = "AI 서버에서 전달한 오류 상세 내용", example = "요청 형식이 올바르지 않습니다.")
                String detail) {}
