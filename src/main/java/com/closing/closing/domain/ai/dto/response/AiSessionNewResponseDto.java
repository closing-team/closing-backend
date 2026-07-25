package com.closing.closing.domain.ai.dto.response;

import com.closing.closing.domain.ai.dto.AiMessageDto;
import java.util.List;

public record AiSessionNewResponseDto(
        String sessionId,
        String status,
        int turnCount,
        List<AiMessageDto> messages)
        implements AiSessionDetailResponseDto {}
