package com.closing.closing.domain.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.closing.closing.domain.ai.dto.request.AiSessionTaskUpdateRequestDto;
import com.closing.closing.domain.ai.dto.response.AiGeneratedTaskDto;
import com.closing.closing.domain.ai.entity.AiSession;
import com.closing.closing.domain.ai.entity.AiSessionStatus;
import com.closing.closing.domain.ai.repository.AiSessionRepository;
import com.closing.closing.domain.task.repository.TaskRepository;
import com.closing.closing.domain.user.repository.UserRepository;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class AiSessionServiceTest {

    private static final Long USER_ID = 1L;
    private static final String SESSION_ID = "session-1";
    private static final String TEMP_ID = "task-1";

    @Mock private AiSessionRepository aiSessionRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;

    private AiSessionService aiSessionService;

    @BeforeEach
    void setUp() {
        WebClient aiWebClient = mock(WebClient.class);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        aiSessionService =
                new AiSessionService(
                        aiWebClient, aiSessionRepository, taskRepository, userRepository, objectMapper);
    }

    private AiSession existingSessionWithTask(AiGeneratedTaskDto task) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        return AiSession.builder()
                .sessionId(SESSION_ID)
                .userId(USER_ID)
                .status(AiSessionStatus.GENERATED)
                .messages("[]")
                .turnCount(1)
                .generatedTasks(objectMapper.writeValueAsString(List.of(task)))
                .build();
    }

    @Test
    @DisplayName("전체 필드를 전달하면 모든 필드가 요청 값으로 반영된다")
    void updateTask_Success_WhenAllFieldsProvided() throws Exception {
        // given
        AiGeneratedTaskDto existingTask =
                new AiGeneratedTaskDto(
                        TEMP_ID,
                        "원래 제목",
                        LocalDate.of(2026, 8, 20),
                        LocalTime.of(10, 0),
                        LocalDate.of(2026, 8, 20),
                        LocalTime.of(11, 0),
                        "원래 메모");
        AiSession aiSession = existingSessionWithTask(existingTask);
        when(aiSessionRepository.findBySessionId(SESSION_ID)).thenReturn(Optional.of(aiSession));

        AiSessionTaskUpdateRequestDto request =
                new AiSessionTaskUpdateRequestDto(
                        "제목 변경",
                        LocalDate.of(2026, 8, 21),
                        LocalTime.of(12, 0),
                        LocalDate.of(2026, 8, 21),
                        LocalTime.of(13, 0),
                        "메모 변경");

        // when
        AiGeneratedTaskDto result = aiSessionService.updateTask(USER_ID, SESSION_ID, TEMP_ID, request);

        // then
        assertEquals("제목 변경", result.title());
        assertEquals(LocalDate.of(2026, 8, 21), result.startDate());
        assertEquals(LocalTime.of(12, 0), result.startTime());
        assertEquals(LocalDate.of(2026, 8, 21), result.endDate());
        assertEquals(LocalTime.of(13, 0), result.endTime());
        assertEquals("메모 변경", result.memo());
        verify(aiSessionRepository).save(any(AiSession.class));
    }

    @Test
    @DisplayName("title만 전달하면 title만 반영되고 나머지 필드는 기존 값이 유지된다")
    void updateTask_Success_WhenOnlyTitleProvided() throws Exception {
        // given
        AiGeneratedTaskDto existingTask =
                new AiGeneratedTaskDto(
                        TEMP_ID,
                        "원래 제목",
                        LocalDate.of(2026, 8, 20),
                        LocalTime.of(10, 0),
                        LocalDate.of(2026, 8, 20),
                        LocalTime.of(11, 0),
                        "원래 메모");
        AiSession aiSession = existingSessionWithTask(existingTask);
        when(aiSessionRepository.findBySessionId(SESSION_ID)).thenReturn(Optional.of(aiSession));

        AiSessionTaskUpdateRequestDto request =
                new AiSessionTaskUpdateRequestDto("제목만 변경", null, null, null, null, null);

        // when
        AiGeneratedTaskDto result = aiSessionService.updateTask(USER_ID, SESSION_ID, TEMP_ID, request);

        // then
        assertEquals("제목만 변경", result.title());
        assertEquals(LocalDate.of(2026, 8, 20), result.startDate());
        assertEquals(LocalTime.of(10, 0), result.startTime());
        assertEquals(LocalDate.of(2026, 8, 20), result.endDate());
        assertEquals(LocalTime.of(11, 0), result.endTime());
        assertEquals("원래 메모", result.memo());
    }

    @Test
    @DisplayName("title이 비어있으면 나머지 필드가 채워져 있어도 수정에 실패한다")
    void updateTask_Fail_WhenTitleIsBlank() throws Exception {
        // given
        AiGeneratedTaskDto existingTask =
                new AiGeneratedTaskDto(
                        TEMP_ID,
                        "원래 제목",
                        LocalDate.of(2026, 8, 20),
                        LocalTime.of(10, 0),
                        LocalDate.of(2026, 8, 20),
                        LocalTime.of(11, 0),
                        "원래 메모");
        AiSession aiSession = existingSessionWithTask(existingTask);
        when(aiSessionRepository.findBySessionId(SESSION_ID)).thenReturn(Optional.of(aiSession));

        AiSessionTaskUpdateRequestDto request =
                new AiSessionTaskUpdateRequestDto(
                        "   ",
                        LocalDate.of(2026, 8, 21),
                        LocalTime.of(12, 0),
                        LocalDate.of(2026, 8, 21),
                        LocalTime.of(13, 0),
                        "메모 변경");

        // when & then
        CustomException exception =
                assertThrows(
                        CustomException.class,
                        () -> aiSessionService.updateTask(USER_ID, SESSION_ID, TEMP_ID, request));

        assertEquals(ErrorCode.AI_EMPTY_TASK_TITLE, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 임시 일정 ID로 수정 시 AI_TEMP_TASK404 예외 발생")
    void updateTask_Fail_WhenTempTaskNotFound() throws Exception {
        // given
        AiGeneratedTaskDto existingTask =
                new AiGeneratedTaskDto(
                        TEMP_ID,
                        "원래 제목",
                        LocalDate.of(2026, 8, 20),
                        LocalTime.of(10, 0),
                        LocalDate.of(2026, 8, 20),
                        LocalTime.of(11, 0),
                        "원래 메모");
        AiSession aiSession = existingSessionWithTask(existingTask);
        when(aiSessionRepository.findBySessionId(SESSION_ID)).thenReturn(Optional.of(aiSession));

        AiSessionTaskUpdateRequestDto request =
                new AiSessionTaskUpdateRequestDto("제목", null, null, null, null, null);

        // when & then
        CustomException exception =
                assertThrows(
                        CustomException.class,
                        () ->
                                aiSessionService.updateTask(
                                        USER_ID, SESSION_ID, "존재하지-않는-id", request));

        assertEquals(ErrorCode.AI_TEMP_TASK_NOT_FOUND, exception.getErrorCode());
    }
}
