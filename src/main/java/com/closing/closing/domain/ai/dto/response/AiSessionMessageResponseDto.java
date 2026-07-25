package com.closing.closing.domain.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "AI 세션 메시지 전송 응답")
public record AiSessionMessageResponseDto(
        @Schema(description = "AI 응답 메시지. 일정이 생성되어 대화가 종료된 경우 null입니다.", example = "몇 명의 직원이 있나요?")
                String aiMessage,
        @Schema(description = "현재까지 진행된 대화 턴 수", example = "2") int turnCount,
        @Schema(description = "이번 응답으로 일정 생성이 완료되어 대화가 종료되었는지 여부", example = "false")
                boolean isFinal,
        @Schema(description = "AI가 생성한 임시 일정 목록. 아직 생성되지 않았다면 null입니다.")
                List<AiGeneratedTaskDto> generatedTasks) {}
