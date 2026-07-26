package com.closing.closing.domain.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "AI 서버 일정 생성 응답 (내부 연동용)")
public record AiGenerateResponseDto(
        @Schema(description = "이번 응답으로 일정 생성이 완료되었는지 여부", example = "false") boolean isFinal,
        @Schema(description = "AI 응답 메시지", example = "몇 명의 직원이 있나요?") String aiMessage,
        @Schema(description = "AI가 생성한 일정 목록") List<AiGenerateTaskDto> tasks) {}
