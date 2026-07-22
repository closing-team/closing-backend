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

    @Builder
    public record UpdateTaskResultDTO(
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
            OffsetDateTime updatedAt
    ) {
        public static UpdateTaskResultDTO from(Task task) {
            return UpdateTaskResultDTO.builder()
                    .taskId(task.getId())
                    .title(task.getTitle())
                    .startDate(task.getStartDate())
                    .endDate(task.getEndDate())
                    .startTime(task.getStartTime())
                    .endTime(task.getEndTime())
                    .isCompleted(task.isCompleted())
                    .source(task.getSource().name().toLowerCase())
                    .updatedAt(task.getUpdatedAt() != null ? task.getUpdatedAt().atOffset(ZoneOffset.of("+09:00")) : null)
                    .build();
        }
    }

    @Builder
    public record TaskDetailDTO(
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

            String description,
            boolean isCompleted,
            String source,

            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssxxx")
            OffsetDateTime createdAt,

            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssxxx")
            OffsetDateTime updatedAt
    ) {
        public static TaskDetailDTO from(Task task) {
            return TaskDetailDTO.builder()
                    .taskId(task.getId())
                    .title(task.getTitle())
                    .startDate(task.getStartDate())
                    .endDate(task.getEndDate())
                    .startTime(task.getStartTime())
                    .endTime(task.getEndTime())
                    .description(task.getDescription())
                    .isCompleted(task.isCompleted())
                    .source(task.getSource().name().toLowerCase())
                    .createdAt(task.getCreatedAt().atOffset(ZoneOffset.of("+09:00")))
                    .updatedAt(task.getUpdatedAt() != null ? task.getUpdatedAt().atOffset(ZoneOffset.of("+09:00")) : null)
                    .build();
        }
    }

    @Builder
    public record CompleteTaskResultDTO(
            Long taskId,
            boolean isCompleted,

            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssxxx")
            OffsetDateTime updatedAt
    ) {
        public static CompleteTaskResultDTO from(Task task) {
            return CompleteTaskResultDTO.builder()
                    .taskId(task.getId())
                    .isCompleted(task.isCompleted())
                    .updatedAt(task.getUpdatedAt() != null ? task.getUpdatedAt().atOffset(ZoneOffset.of("+09:00")) : null)
                    .build();
        }
    }

    @Builder
    public record HomeDTO(
            SummaryDTO summary,
            java.util.List<CalendarTaskDTO> calendar
    ) {
    }

    @Builder
    public record SummaryDTO(
            int totalCount,
            int completedCount,
            double progressRate
    ) {
    }

    @Builder
    public record CalendarTaskDTO(
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
            String source
    ) {
        public static CalendarTaskDTO from(Task task) {
            return CalendarTaskDTO.builder()
                    .taskId(task.getId())
                    .title(task.getTitle())
                    .startDate(task.getStartDate())
                    .endDate(task.getEndDate())
                    .startTime(task.getStartTime())
                    .endTime(task.getEndTime())
                    .isCompleted(task.isCompleted())
                    .source(task.getSource().name().toLowerCase())
                    .build();
        }
    }
}
