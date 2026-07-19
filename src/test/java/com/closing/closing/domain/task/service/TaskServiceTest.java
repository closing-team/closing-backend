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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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

    @Test
    @DisplayName("일정 수정 성공")
    void updateTask_Success() throws Exception {
        // given
        Long taskId = 1L;
        TaskReqDTO.UpdateTaskDTO request = new TaskReqDTO.UpdateTaskDTO(
                "제목 변경",
                LocalDate.of(2026, 7, 16),
                LocalDate.of(2026, 7, 17),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                "설명 변경"
        );

        Task existingTask = Task.builder()
                .registration(null)
                .title("원래 제목")
                .startDate(LocalDate.of(2026, 7, 15))
                .endDate(LocalDate.of(2026, 7, 15))
                .startTime(LocalTime.of(10, 30))
                .endTime(LocalTime.of(11, 30))
                .source(TaskSource.MANUAL)
                .description("원래 설명")
                .build();
        setCreatedAt(existingTask, LocalDateTime.of(2026, 7, 15, 10, 0));
        setUpdatedAt(existingTask, LocalDateTime.of(2026, 7, 16, 10, 0));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // when
        TaskResDTO.UpdateTaskResultDTO result = taskService.updateTask(taskId, request);

        // then
        assertEquals("제목 변경", result.title());
        assertEquals(LocalDate.of(2026, 7, 16), result.startDate());
        assertEquals(LocalDate.of(2026, 7, 17), result.endDate());
        assertEquals(LocalTime.of(12, 0), result.startTime());
        assertEquals("설명 변경", existingTask.getDescription());
    }

    @Test
    @DisplayName("존재하지 않는 일정 수정 시 TASK404 예외 발생")
    void updateTask_Fail_WhenTaskNotFound() {
        // given
        Long taskId = 999L;
        TaskReqDTO.UpdateTaskDTO request = new TaskReqDTO.UpdateTaskDTO(
                "제목 변경", null, null, null, null, null
        );

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // when & then
        TaskException exception = assertThrows(TaskException.class,
                () -> taskService.updateTask(taskId, request));

        assertEquals(TaskErrorCode.TASK_NOT_FOUND, exception.getTaskErrorCode());
    }

    @Test
    @DisplayName("일정 삭제 성공")
    void deleteTask_Success() {
        // given
        Long taskId = 1L;
        Task existingTask = Task.builder()
                .registration(null)
                .title("삭제할 일정")
                .startDate(LocalDate.of(2026, 7, 15))
                .endDate(LocalDate.of(2026, 7, 15))
                .startTime(LocalTime.of(10, 30))
                .endTime(LocalTime.of(11, 30))
                .source(TaskSource.MANUAL)
                .description("설명")
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // when & then
        assertDoesNotThrow(() -> taskService.deleteTask(taskId));
        verify(taskRepository).delete(existingTask);
    }

    @Test
    @DisplayName("존재하지 않는 일정 삭제 시 TASK404 예외 발생")
    void deleteTask_Fail_WhenTaskNotFound() {
        // given
        Long taskId = 999L;

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // when & then
        TaskException exception = assertThrows(TaskException.class,
                () -> taskService.deleteTask(taskId));

        assertEquals(TaskErrorCode.TASK_NOT_FOUND, exception.getTaskErrorCode());
    }

    @Test
    @DisplayName("일정 상세 조회 성공")
    void getTask_Success() throws Exception {
        // given
        Long taskId = 1L;
        Task existingTask = Task.builder()
                .registration(null)
                .title("매장 철거 업체 미팅")
                .startDate(LocalDate.of(2026, 7, 15))
                .endDate(LocalDate.of(2026, 7, 16))
                .startTime(LocalTime.of(10, 30))
                .endTime(LocalTime.of(12, 0))
                .source(TaskSource.MANUAL)
                .description("업체 준비 서류 필요")
                .build();
        setCreatedAt(existingTask, LocalDateTime.of(2026, 7, 4, 13, 0));
        setUpdatedAt(existingTask, LocalDateTime.of(2026, 7, 4, 13, 5));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // when
        TaskResDTO.TaskDetailDTO result = taskService.getTask(taskId);

        // then
        assertEquals("매장 철거 업체 미팅", result.title());
        assertEquals(LocalDate.of(2026, 7, 15), result.startDate());
        assertEquals(LocalDate.of(2026, 7, 16), result.endDate());
        assertEquals(LocalTime.of(10, 30), result.startTime());
        assertEquals(LocalTime.of(12, 0), result.endTime());
        assertEquals("업체 준비 서류 필요", result.description());
        assertFalse(result.isCompleted());
        assertEquals("manual", result.source());
        assertNotNull(result.createdAt());
        assertNotNull(result.updatedAt());
    }

    @Test
    @DisplayName("존재하지 않는 일정 조회 시 TASK404 예외 발생")
    void getTask_Fail_WhenTaskNotFound() {
        // given
        Long taskId = 999L;

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // when & then
        TaskException exception = assertThrows(TaskException.class,
                () -> taskService.getTask(taskId));

        assertEquals(TaskErrorCode.TASK_NOT_FOUND, exception.getTaskErrorCode());
    }

    private void setCreatedAt(Task task, LocalDateTime time) throws Exception {
        Field field = task.getClass().getSuperclass().getDeclaredField("createdAt");
        field.setAccessible(true);
        field.set(task, time);
    }

    private void setUpdatedAt(Task task, LocalDateTime time) throws Exception {
        Field field = task.getClass().getSuperclass().getDeclaredField("updatedAt");
        field.setAccessible(true);
        field.set(task, time);
    }
}
