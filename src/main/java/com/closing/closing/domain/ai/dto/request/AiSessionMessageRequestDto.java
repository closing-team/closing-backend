package com.closing.closing.domain.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 세션 메시지 전송 요청")
public record AiSessionMessageRequestDto(
        @Schema(
                description = "세션에 이어서 전달할 사용자 메시지",
                example = "직원은 3명이고 다음 달 20일에 마지막으로 영업할 예정이야",
                requiredMode = Schema.RequiredMode.REQUIRED)
                String message) {}
