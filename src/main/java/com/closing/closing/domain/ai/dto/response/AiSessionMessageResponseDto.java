package com.closing.closing.domain.ai.dto.response;

import java.util.List;

public record AiSessionMessageResponseDto(
        String aiMessage,
        int turnCount,
        boolean isFinal,
        List<AiGeneratedTaskDto> generatedTasks) {}
