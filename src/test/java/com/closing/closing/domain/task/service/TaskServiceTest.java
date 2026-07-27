package com.closing.closing.domain.task.service;

import com.closing.closing.domain.business.entity.BusinessRegistration;
import com.closing.closing.domain.task.dto.TaskReqDTO;
import com.closing.closing.domain.task.dto.TaskResDTO;
import com.closing.closing.domain.task.entity.Task;
import com.closing.closing.domain.task.entity.TaskSource;
import com.closing.closing.domain.task.repository.TaskRepository;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
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
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private BusinessRegistration businessRegistration;

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
                .registration(businessRegistration)
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

        when(taskRepository.findBusinessRegistrationByUserId(USER_ID))
                .thenReturn(Optional.of(businessRegistration));
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // when
        TaskResDTO.CreateTaskResultDTO result =
                taskService.createTask(USER_ID, request);

        // then
        verify(taskRepository).save(argThat(task -> task.getRegistration() == businessRegistration));
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
        CustomException exception = assertThrows(CustomException.class,
                () -> taskService.createTask(USER_ID, request));

        assertEquals(ErrorCode.TASK_TITLE_BLANK, exception.getErrorCode());
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
        CustomException exception = assertThrows(CustomException.class,
                () -> taskService.createTask(USER_ID, request));

        assertEquals(ErrorCode.TASK_TITLE_BLANK, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자의 사업자 등록 정보를 찾을 수 없으면 일정 생성 실패")
    void createTask_Fail_WhenBusinessRegistrationNotFound() {
        // given
        TaskReqDTO.CreateTaskDTO request = new TaskReqDTO.CreateTaskDTO(
                "일정",
                LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 7, 15),
                null,
                null,
                null
        );
        when(taskRepository.findBusinessRegistrationByUserId(USER_ID))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> taskService.createTask(USER_ID, request));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
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

        when(taskRepository.findByIdAndRegistration_User_Id(taskId, USER_ID))
                .thenReturn(Optional.of(existingTask));

        // when
        TaskResDTO.UpdateTaskResultDTO result =
                taskService.updateTask(USER_ID, taskId, request);

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

        when(taskRepository.findByIdAndRegistration_User_Id(taskId, USER_ID))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> taskService.updateTask(USER_ID, taskId, request));

        assertEquals(ErrorCode.TASK_NOT_FOUND, exception.getErrorCode());
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

        when(taskRepository.findByIdAndRegistration_User_Id(taskId, USER_ID))
                .thenReturn(Optional.of(existingTask));

        // when & then
        assertDoesNotThrow(() -> taskService.deleteTask(USER_ID, taskId));
        verify(taskRepository).delete(existingTask);
    }

    @Test
    @DisplayName("존재하지 않는 일정 삭제 시 TASK404 예외 발생")
    void deleteTask_Fail_WhenTaskNotFound() {
        // given
        Long taskId = 999L;

        when(taskRepository.findByIdAndRegistration_User_Id(taskId, USER_ID))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> taskService.deleteTask(USER_ID, taskId));

        assertEquals(ErrorCode.TASK_NOT_FOUND, exception.getErrorCode());
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

        when(taskRepository.findByIdAndRegistration_User_Id(taskId, USER_ID))
                .thenReturn(Optional.of(existingTask));

        // when
        TaskResDTO.TaskDetailDTO result =
                taskService.getTask(USER_ID, taskId);

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

        when(taskRepository.findByIdAndRegistration_User_Id(taskId, USER_ID))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> taskService.getTask(USER_ID, taskId));

        assertEquals(ErrorCode.TASK_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("다른 사용자의 일정 조회 시 TASK404 예외 발생")
    void getTask_Fail_WhenTaskBelongsToAnotherUser() {
        // given
        Long taskId = 1L;
        when(taskRepository.findByIdAndRegistration_User_Id(taskId, USER_ID))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> taskService.getTask(USER_ID, taskId));

        assertEquals(ErrorCode.TASK_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("일정 완료 처리 성공 - 완료로 변경")
    void completeTask_Success_MarkAsCompleted() throws Exception {
        // given
        Long taskId = 1L;
        TaskReqDTO.CompleteTaskDTO request = new TaskReqDTO.CompleteTaskDTO(true);

        Task existingTask = Task.builder()
                .registration(null)
                .title("미완료 일정")
                .startDate(LocalDate.of(2026, 7, 15))
                .endDate(LocalDate.of(2026, 7, 15))
                .source(TaskSource.MANUAL)
                .build();
        setUpdatedAt(existingTask, LocalDateTime.of(2026, 7, 15, 14, 0));

        when(taskRepository.findByIdAndRegistration_User_Id(taskId, USER_ID))
                .thenReturn(Optional.of(existingTask));

        // when
        TaskResDTO.CompleteTaskResultDTO result =
                taskService.completeTask(USER_ID, taskId, request);

        // then
        assertTrue(result.isCompleted());
    }

    @Test
    @DisplayName("일정 완료 처리 성공 - 미완료로 되돌리기")
    void completeTask_Success_MarkAsIncomplete() throws Exception {
        // given
        Long taskId = 1L;
        TaskReqDTO.CompleteTaskDTO request = new TaskReqDTO.CompleteTaskDTO(false);

        Task existingTask = Task.builder()
                .registration(null)
                .title("완료된 일정")
                .startDate(LocalDate.of(2026, 7, 15))
                .endDate(LocalDate.of(2026, 7, 15))
                .source(TaskSource.MANUAL)
                .build();
        existingTask.complete(true); // 먼저 완료 상태로 세팅
        setUpdatedAt(existingTask, LocalDateTime.of(2026, 7, 15, 14, 0));

        when(taskRepository.findByIdAndRegistration_User_Id(taskId, USER_ID))
                .thenReturn(Optional.of(existingTask));

        // when
        TaskResDTO.CompleteTaskResultDTO result =
                taskService.completeTask(USER_ID, taskId, request);

        // then
        assertFalse(result.isCompleted());
    }

    @Test
    @DisplayName("존재하지 않는 일정 완료 처리 시 TASK404 예외 발생")
    void completeTask_Fail_WhenTaskNotFound() {
        // given
        Long taskId = 999L;
        TaskReqDTO.CompleteTaskDTO request = new TaskReqDTO.CompleteTaskDTO(true);

        when(taskRepository.findByIdAndRegistration_User_Id(taskId, USER_ID))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> taskService.completeTask(USER_ID, taskId, request));

        assertEquals(ErrorCode.TASK_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("홈 화면 조회 성공 - 진행도는 전체 기준, 캘린더는 월별 기준")
    void getHome_Success_WithTasks() {
        // given
        YearMonth yearMonth = YearMonth.of(2026, 7);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        Task julyTask = Task.builder()
                .registration(null)
                .title("7월 일정")
                .startDate(LocalDate.of(2026, 7, 10))
                .endDate(LocalDate.of(2026, 7, 10))
                .source(TaskSource.MANUAL)
                .build();

        // 현재 사용자의 전체 Task: 2개 (1개 완료)
        when(taskRepository.countByRegistration_User_Id(USER_ID)).thenReturn(2L);
        when(taskRepository.countByRegistration_User_IdAndIsCompletedTrue(USER_ID))
                .thenReturn(1L);
        // 7월 캘린더: 1개만 해당
        when(taskRepository.findAllByUserIdAndMonth(USER_ID, startOfMonth, endOfMonth))
                .thenReturn(List.of(julyTask));

        // when
        TaskResDTO.HomeDTO result = taskService.getHome(USER_ID, yearMonth);

        // then - 진행도: 전체 기준 (2개 중 1개 완료 = 50%)
        assertEquals(2, result.summary().totalCount());
        assertEquals(1, result.summary().completedCount());
        assertEquals(50.0, result.summary().progressRate());

        // then - 캘린더: 7월 기준 (1개만)
        assertEquals(1, result.calendar().size());
        assertEquals("7월 일정", result.calendar().get(0).title());
    }

    @Test
    @DisplayName("홈 화면 조회 성공 - 일정이 없는 경우")
    void getHome_Success_NoTasks() {
        // given
        YearMonth yearMonth = YearMonth.of(2026, 8);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        when(taskRepository.countByRegistration_User_Id(USER_ID)).thenReturn(0L);
        when(taskRepository.countByRegistration_User_IdAndIsCompletedTrue(USER_ID))
                .thenReturn(0L);
        when(taskRepository.findAllByUserIdAndMonth(USER_ID, startOfMonth, endOfMonth))
                .thenReturn(List.of());

        // when
        TaskResDTO.HomeDTO result = taskService.getHome(USER_ID, yearMonth);

        // then
        assertEquals(0, result.summary().totalCount());
        assertEquals(0, result.summary().completedCount());
        assertEquals(0.0, result.summary().progressRate());
        assertTrue(result.calendar().isEmpty());
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
