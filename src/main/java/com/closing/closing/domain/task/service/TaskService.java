package com.closing.closing.domain.task.service;

import com.closing.closing.domain.task.dto.TaskReqDTO;
import com.closing.closing.domain.task.dto.TaskResDTO;
import com.closing.closing.domain.task.entity.Task;
import com.closing.closing.domain.task.entity.TaskSource;
import com.closing.closing.domain.task.exception.TaskException;
import com.closing.closing.domain.task.exception.code.TaskErrorCode;
import com.closing.closing.domain.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional
    public TaskResDTO.CreateTaskResultDTO createTask(TaskReqDTO.CreateTaskDTO request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new TaskException(TaskErrorCode.TASK_TITLE_BLANK);
        }

        // TODO: 인증 추가 후 토큰에서 registration 정보 추출 필요
        Task task = Task.builder()
                .registration(null)
                .title(request.title())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .source(TaskSource.MANUAL)
                .description(request.description())
                .build();

        Task savedTask = taskRepository.save(task);

        return TaskResDTO.CreateTaskResultDTO.from(savedTask);
    }

    @Transactional
    public TaskResDTO.UpdateTaskResultDTO updateTask(Long taskId, TaskReqDTO.UpdateTaskDTO request) {
        // TODO: 인증 추가 후 본인의 일정인지 확인 필요

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(TaskErrorCode.TASK_NOT_FOUND));

        task.update(
                request.title(),
                request.startDate(),
                request.endDate(),
                request.startTime(),
                request.endTime(),
                request.description()
        );

        return TaskResDTO.UpdateTaskResultDTO.from(task);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        // TODO: 인증 추가 후 본인의 일정인지 확인 필요

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(TaskErrorCode.TASK_NOT_FOUND));

        taskRepository.delete(task);
    }

    public TaskResDTO.TaskDetailDTO getTask(Long taskId) {
        // TODO: 인증 추가 후 본인의 일정인지 확인 필요

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(TaskErrorCode.TASK_NOT_FOUND));

        return TaskResDTO.TaskDetailDTO.from(task);
    }

    @Transactional
    public TaskResDTO.CompleteTaskResultDTO completeTask(Long taskId, TaskReqDTO.CompleteTaskDTO request) {
        // TODO: 인증 추가 후 본인의 일정인지 확인 필요

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(TaskErrorCode.TASK_NOT_FOUND));

        task.complete(request.isCompleted());

        return TaskResDTO.CompleteTaskResultDTO.from(task);
    }

    public TaskResDTO.HomeDTO getHome(YearMonth yearMonth) {
        // TODO: 인증 추가 후 본인의 일정만 조회하도록 변경 필요

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Task> tasks = taskRepository.findAllByMonth(startDate, endDate);

        int totalCount = tasks.size();
        int completedCount = (int) tasks.stream().filter(Task::isCompleted).count();
        double progressRate = totalCount == 0 ? 0.0
                : Math.round((double) completedCount / totalCount * 1000) / 10.0;

        TaskResDTO.SummaryDTO summary = TaskResDTO.SummaryDTO.builder()
                .totalCount(totalCount)
                .completedCount(completedCount)
                .progressRate(progressRate)
                .build();

        List<TaskResDTO.CalendarTaskDTO> calendar = tasks.stream()
                .map(TaskResDTO.CalendarTaskDTO::from)
                .toList();

        return TaskResDTO.HomeDTO.builder()
                .summary(summary)
                .calendar(calendar)
                .build();
    }
}
