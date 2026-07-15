package com.closing.closing.domain.ai.dto;

import java.util.List;

public record AiSessionNewResponseDto(
        String sessionId,
        String status,
        int turnCount,
        List<AiMessageDto> messages)
        implements AiSessionDetailResponseDto {}
