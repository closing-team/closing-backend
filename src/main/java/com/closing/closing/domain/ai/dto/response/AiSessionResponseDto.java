package com.closing.closing.domain.ai.dto.response;

import java.util.List;

public record AiSessionResponseDto(
        String sessionId,
        String status,
        String aiMessage,
        int turnCount,
        List<AiGeneratedTaskDto> generatedTasks) {}
