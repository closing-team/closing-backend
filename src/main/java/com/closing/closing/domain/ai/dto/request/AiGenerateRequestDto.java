package com.closing.closing.domain.ai.dto.request;

import com.closing.closing.domain.ai.dto.AiMessageDto;
import java.util.List;

public record AiGenerateRequestDto(List<AiMessageDto> messages, int turnCount) {}
