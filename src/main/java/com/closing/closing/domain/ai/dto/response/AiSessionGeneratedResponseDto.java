package com.closing.closing.domain.ai.dto.response;

import java.util.List;

public record AiSessionGeneratedResponseDto(
        String sessionId,
        String status,
        List<AiGeneratedTaskDto> generatedTasks)
        implements AiSessionDetailResponseDto {}
