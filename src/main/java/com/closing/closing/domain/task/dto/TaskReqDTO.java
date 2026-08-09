package com.closing.closing.domain.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class TaskReqDTO {

    public record CreateTaskDTO(

            @NotBlank(message = "일정 제목이 비어있습니다.")
            String title,

            @NotNull(message = "시작 날짜는 필수입니다.")
            LocalDate startDate,

            @NotNull(message = "종료 날짜는 필수입니다.")
            LocalDate endDate,

            LocalTime startTime,

            LocalTime endTime,

            String description
    ) {
    }

    @Schema(description = "null이 아닌 필드만 수정되며, 누락되었거나 null인 필드는 기존 값이 그대로 유지됩니다.")
    public record UpdateTaskDTO(

            @Schema(description = "일정 제목 (null이거나 미전달 시 기존 값 유지)", example = "매장 철거 업체 미팅")
            String title,

            @Schema(description = "시작 날짜 (null이거나 미전달 시 기존 값 유지)", example = "2026-07-16")
            LocalDate startDate,

            @Schema(description = "종료 날짜 (null이거나 미전달 시 기존 값 유지)", example = "2026-07-17")
            LocalDate endDate,

            @Schema(description = "시작 시간 (null이거나 미전달 시 기존 값 유지)", example = "12:00:00")
            LocalTime startTime,

            @Schema(description = "종료 시간 (null이거나 미전달 시 기존 값 유지)", example = "13:00:00")
            LocalTime endTime,

            @Schema(description = "상세 설명 (null이거나 미전달 시 기존 값 유지)", example = "A업체, B업체 견적 비교")
            String description
    ) {
    }

    public record CompleteTaskDTO(
            @NotNull(message = "완료 여부는 필수입니다.")
            Boolean isCompleted
    ) {
    }
}
