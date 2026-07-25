package com.closing.closing.domain.ai.dto.request;

import com.closing.closing.domain.ai.dto.AiMessageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "AI 서버 일정 생성 요청 (내부 연동용)")
public record AiGenerateRequestDto(
        @Schema(description = "지금까지 주고받은 대화 메시지 목록") List<AiMessageDto> messages,
        @Schema(description = "현재까지 진행된 대화 턴 수", example = "1") int turnCount) {}
