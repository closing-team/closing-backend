package com.closing.closing.domain.task.service;

import com.closing.closing.domain.business.entity.BusinessRegistration;
import com.closing.closing.domain.task.dto.TaskReqDTO;
import com.closing.closing.domain.task.dto.TaskResDTO;
import com.closing.closing.domain.task.entity.Task;
import com.closing.closing.domain.task.entity.TaskSource;
import com.closing.closing.domain.task.repository.TaskRepository;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
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
    public TaskResDTO.CreateTaskResultDTO createTask(
            Long userId,
            TaskReqDTO.CreateTaskDTO request
    ) {
        if (request.title() == null || request.title().isBlank()) {
            throw new CustomException(ErrorCode.TASK_TITLE_BLANK);
        }

        BusinessRegistration registration = taskRepository.findBusinessRegistrationByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Task task = Task.builder()
                .registration(registration)
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
    public TaskResDTO.UpdateTaskResultDTO updateTask(
            Long userId,
            Long taskId,
            TaskReqDTO.UpdateTaskDTO request
    ) {
        Task task = taskRepository.findByIdAndRegistration_User_Id(taskId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));

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
    public void deleteTask(Long userId, Long taskId) {
        Task task = taskRepository.findByIdAndRegistration_User_Id(taskId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));

        taskRepository.delete(task);
    }

    public TaskResDTO.TaskDetailDTO getTask(Long userId, Long taskId) {
        Task task = taskRepository.findByIdAndRegistration_User_Id(taskId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));

        return TaskResDTO.TaskDetailDTO.from(task);
    }

    @Transactional
    public TaskResDTO.CompleteTaskResultDTO completeTask(
            Long userId,
            Long taskId,
            TaskReqDTO.CompleteTaskDTO request
    ) {
        Task task = taskRepository.findByIdAndRegistration_User_Id(taskId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));

        task.complete(request.isCompleted());

        return TaskResDTO.CompleteTaskResultDTO.from(task);
    }

    public TaskResDTO.HomeDTO getHome(Long userId, YearMonth yearMonth) {
        long totalCount = taskRepository.countByRegistration_User_Id(userId);
        long completedCount = taskRepository.countByRegistration_User_IdAndIsCompletedTrue(userId);
        double progressRate = totalCount == 0 ? 0.0
                : Math.round((double) completedCount / totalCount * 1000) / 10.0;

        TaskResDTO.SummaryDTO summary = TaskResDTO.SummaryDTO.builder()
                .totalCount(Math.toIntExact(totalCount))
                .completedCount(Math.toIntExact(completedCount))
                .progressRate(progressRate)
                .build();

        // 캘린더: 해당 월 기준
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        List<Task> monthlyTasks =
                taskRepository.findAllByUserIdAndMonth(userId, startDate, endDate);

        List<TaskResDTO.CalendarTaskDTO> calendar = monthlyTasks.stream()
                .map(TaskResDTO.CalendarTaskDTO::from)
                .toList();

        return TaskResDTO.HomeDTO.builder()
                .summary(summary)
                .calendar(calendar)
                .build();
    }
}
