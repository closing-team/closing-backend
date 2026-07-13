package com.closing.closing.domain.ai.dto;

public record AiSessionResponseDto(String sessionId, String status, String aiMessage, int turnCount) {}
