package com.closing.closing.domain.ai.dto;

import java.util.List;

public record AiGenerateResponseDto(boolean isFinal, String aiMessage, List<AiGenerateTaskDto> tasks) {}
