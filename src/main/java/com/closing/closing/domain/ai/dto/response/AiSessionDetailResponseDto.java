package com.closing.closing.domain.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

// status별로 응답 필드 구성이 달라 구현체 분리
@Schema(
        description = "AI 세션 조회 응답. 세션 상태에 따라 실제 구현체가 달라집니다.",
        oneOf = {
            AiSessionNewResponseDto.class,
            AiSessionGeneratedResponseDto.class,
            AiSessionConfirmedResponseDto.class
        })
public interface AiSessionDetailResponseDto {}
