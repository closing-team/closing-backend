package com.closing.closing.domain.task.dto;

import com.closing.closing.domain.task.entity.Task;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TaskResDTO {

    @Builder
    public record CreateTaskResultDTO(
            Long taskId,
            String title,

            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate startDate,

            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate endDate,

            @JsonFormat(pattern = "HH:mm")
            LocalTime startTime,

            @JsonFormat(pattern = "HH:mm")
            LocalTime endTime,

            boolean isCompleted,
            String source,

            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssxxx")
            OffsetDateTime createdAt
    ) {
        public static CreateTaskResultDTO from(Task task) {
            return CreateTaskResultDTO.builder()
                    .taskId(task.getId())
                    .title(task.getTitle())
                    .startDate(task.getStartDate())
                    .endDate(task.getEndDate())
                    .startTime(task.getStartTime())
                    .endTime(task.getEndTime())
                    .isCompleted(task.isCompleted())
                    .source(task.getSource().name().toLowerCase())
                    .createdAt(task.getCreatedAt().atOffset(ZoneOffset.of("+09:00")))
                    .build();
        }
    }
}
