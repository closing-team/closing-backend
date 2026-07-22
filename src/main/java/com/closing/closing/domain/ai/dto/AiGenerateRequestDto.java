package com.closing.closing.domain.ai.dto;

import java.util.List;

public record AiGenerateRequestDto(List<AiMessageDto> messages, int turnCount) {}
