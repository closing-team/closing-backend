package com.closing.closing.domain.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class TaskReqDTO {

    public record CreateTaskDTO(

            @NotBlank(message = "일정/할일 제목이 비어있습니다.")
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
}
