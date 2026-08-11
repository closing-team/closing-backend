package com.closing.closing.domain.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(
        description =
                "AI 생성 임시 일정 수정 요청. title은 필수이며, 그 외 필드는 null이 아닌 필드만 수정되고"
                        + " 누락되었거나 null인 필드는 기존 값이 그대로 유지됩니다.")
public record AiSessionTaskUpdateRequestDto(
        @Schema(
                description = "일정 제목 (필수)",
                example = "폐업 신고서 제출",
                requiredMode = Schema.RequiredMode.REQUIRED)
                String title,
        @Schema(description = "일정 시작 날짜 (null이거나 미전달 시 기존 값 유지)", example = "2026-08-20")
                LocalDate startDate,
        @Schema(description = "일정 시작 시각 (null이거나 미전달 시 기존 값 유지)", example = "10:00:00")
                LocalTime startTime,
        @Schema(description = "일정 종료 날짜 (null이거나 미전달 시 기존 값 유지)", example = "2026-08-20")
                LocalDate endDate,
        @Schema(description = "일정 종료 시각 (null이거나 미전달 시 기존 값 유지)", example = "22:00:00")
                LocalTime endTime,
        @Schema(description = "일정 메모 (null이거나 미전달 시 기존 값 유지)", example = "관할 세무서 방문 필요")
                String memo) {}
