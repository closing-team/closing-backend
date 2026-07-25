package com.closing.closing.domain.ai.dto.response;

import java.util.List;

public record AiSessionConfirmedResponseDto(
        String sessionId,
        String status,
        List<AiConfirmedTaskDto> confirmedTasks)
        implements AiSessionDetailResponseDto {}
