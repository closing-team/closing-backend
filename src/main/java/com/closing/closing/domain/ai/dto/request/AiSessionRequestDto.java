package com.closing.closing.domain.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 세션 시작 요청")
public record AiSessionRequestDto(
        @Schema(
                description = "폐업 일정 생성을 위한 초기 상황 설명",
                example = "다음 달 말에 카페를 폐업하려고 해",
                requiredMode = Schema.RequiredMode.REQUIRED)
                String initialInput) {}
