package com.closing.closing.domain.task.service;

import com.closing.closing.domain.task.dto.TaskReqDTO;
import com.closing.closing.domain.task.dto.TaskResDTO;
import com.closing.closing.domain.task.entity.Task;
import com.closing.closing.domain.task.entity.TaskSource;
import com.closing.closing.domain.task.exception.TaskException;
import com.closing.closing.domain.task.exception.code.TaskErrorCode;
import com.closing.closing.domain.task.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    @DisplayName("일정 생성 성공")
    void createTask_Success() throws Exception {
        // given
        TaskReqDTO.CreateTaskDTO request = new TaskReqDTO.CreateTaskDTO(
                "매장 철거 업체 미팅",
                LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 7, 15),
                LocalTime.of(10, 30),
                LocalTime.of(11, 30),
                "A업체, B업체 견적 비교"
        );

        Task savedTask = Task.builder()
                .registration(null)
                .title("매장 철거 업체 미팅")
                .startDate(LocalDate.of(2026, 7, 15))
                .endDate(LocalDate.of(2026, 7, 15))
                .startTime(LocalTime.of(10, 30))
                .endTime(LocalTime.of(11, 30))
                .source(TaskSource.MANUAL)
                .description("A업체, B업체 견적 비교")
                .build();

        // @CreatedDate 직접 주입
        setCreatedAt(savedTask, LocalDateTime.of(2026, 7, 15, 10, 0));

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // when
        TaskResDTO.CreateTaskResultDTO result = taskService.createTask(request);

        // then
        assertEquals("매장 철거 업체 미팅", result.title());
        assertEquals(LocalDate.of(2026, 7, 15), result.startDate());
        assertEquals("manual", result.source());
        assertFalse(result.isCompleted());
    }

    @Test
    @DisplayName("제목이 null이면 생성 실패")
    void createTask_Fail_WhenTitleIsNull() {
        // given
        TaskReqDTO.CreateTaskDTO request = new TaskReqDTO.CreateTaskDTO(
                null,
                LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 7, 15),
                LocalTime.of(10, 30),
                null,
                null
        );

        // when & then
        TaskException exception = assertThrows(TaskException.class,
                () -> taskService.createTask(request));

        assertEquals(TaskErrorCode.TASK_TITLE_BLANK, exception.getTaskErrorCode());
    }

    @Test
    @DisplayName("제목이 공백이면 생성 실패")
    void createTask_Fail_WhenTitleIsBlank() {
        // given
        TaskReqDTO.CreateTaskDTO request = new TaskReqDTO.CreateTaskDTO(
                "   ",
                LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 7, 15),
                LocalTime.of(10, 30),
                null,
                null
        );

        // when & then
        TaskException exception = assertThrows(TaskException.class,
                () -> taskService.createTask(request));

        assertEquals(TaskErrorCode.TASK_TITLE_BLANK, exception.getTaskErrorCode());
    }

    private void setCreatedAt(Task task, LocalDateTime time) throws Exception {
        Field field = task.getClass().getSuperclass().getDeclaredField("createdAt");
        field.setAccessible(true);
        field.set(task, time);
    }
}
