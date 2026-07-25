package com.closing.closing.domain.ai.service;

import com.closing.closing.domain.ai.dto.AiConfirmedTaskDto;
import com.closing.closing.domain.ai.dto.AiErrorResponseDto;
import com.closing.closing.domain.ai.dto.AiGenerateRequestDto;
import com.closing.closing.domain.ai.dto.AiGenerateResponseDto;
import com.closing.closing.domain.ai.dto.AiGenerateTaskDto;
import com.closing.closing.domain.ai.dto.AiGeneratedTaskDto;
import com.closing.closing.domain.ai.dto.AiMessageDto;
import com.closing.closing.domain.ai.dto.AiSessionConfirmedResponseDto;
import com.closing.closing.domain.ai.dto.AiSessionDetailResponseDto;
import com.closing.closing.domain.ai.dto.AiSessionGeneratedResponseDto;
import com.closing.closing.domain.ai.dto.AiSessionMessageResponseDto;
import com.closing.closing.domain.ai.dto.AiSessionNewResponseDto;
import com.closing.closing.domain.ai.dto.AiSessionRequestDto;
import com.closing.closing.domain.ai.dto.AiSessionResponseDto;
import com.closing.closing.domain.ai.dto.AiSessionTaskUpdateRequestDto;
import com.closing.closing.domain.ai.entity.AiSession;
import com.closing.closing.domain.ai.entity.AiSessionStatus;
import com.closing.closing.domain.ai.repository.AiSessionRepository;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AiSessionService {

    private static final String GENERATE_PATH = "/api/ai/generate";
    private static final String USER_ROLE = "user";
    private static final String AI_ROLE = "ai";
    private static final int INITIAL_TURN_COUNT = 1;

    private final WebClient aiWebClient;
    private final AiSessionRepository aiSessionRepository;
    private final ObjectMapper objectMapper;

    public AiSessionResponseDto createSession(AiSessionRequestDto request) {
        validateInitialInput(request.initialInput());

        List<AiMessageDto> messages = new ArrayList<>();
        messages.add(new AiMessageDto(USER_ROLE, request.initialInput()));

        AiGenerateResponseDto aiResponse = requestGenerate(messages, INITIAL_TURN_COUNT);
        String sessionId = UUID.randomUUID().toString();

        // 첫 턴에 바로 일정이 확정되는 경우
        if (aiResponse.isFinal()) {
            return saveGeneratedSession(sessionId, messages, aiResponse.tasks());
        }
        return saveNewSession(sessionId, messages, aiResponse.aiMessage());
    }

    private AiSessionResponseDto saveNewSession(String sessionId, List<AiMessageDto> messages, String aiMessage) {
        // AI 서버는 세션을 기억 못 하므로 AI응답을 이력에 저장
        List<AiMessageDto> updatedMessages = new ArrayList<>(messages);
        updatedMessages.add(new AiMessageDto(AI_ROLE, aiMessage));

        AiSession aiSession =
                AiSession.builder()
                        .sessionId(sessionId)
                        .status(AiSessionStatus.NEW)
                        .messages(serialize(updatedMessages))
                        .turnCount(INITIAL_TURN_COUNT)
                        .build();
        saveSession(aiSession);

        return new AiSessionResponseDto(
                sessionId, AiSessionStatus.NEW.name(), aiMessage, INITIAL_TURN_COUNT, null);
    }

    private AiSessionResponseDto saveGeneratedSession(
            String sessionId, List<AiMessageDto> messages, List<AiGenerateTaskDto> tasks) {
        List<AiGeneratedTaskDto> generatedTasks = tasks.stream().map(this::assignTempId).toList();

        AiSession aiSession =
                AiSession.builder()
                        .sessionId(sessionId)
                        .status(AiSessionStatus.GENERATED)
                        .messages(serialize(messages))
                        .turnCount(INITIAL_TURN_COUNT)
                        .generatedTasks(serialize(generatedTasks))
                        .build();
        saveSession(aiSession);

        return new AiSessionResponseDto(
                sessionId, AiSessionStatus.GENERATED.name(), null, INITIAL_TURN_COUNT, generatedTasks);
    }

    public AiSessionDetailResponseDto getSession(String sessionId) {
        AiSession aiSession =
                aiSessionRepository
                        .findBySessionId(sessionId)
                        .orElseThrow(() -> new CustomException(ErrorCode.AI_SESSION_NOT_FOUND));

        // status별로 응답 구조가 달라서 분리
        return switch (aiSession.getStatus()) {
            case NEW -> toNewResponse(aiSession);
            case GENERATED -> toGeneratedResponse(aiSession);
            case ALREADY_CONFIRMED -> toConfirmedResponse(aiSession);
        };
    }

    private AiSessionNewResponseDto toNewResponse(AiSession aiSession) {
        List<AiMessageDto> messages = deserialize(aiSession.getMessages(), new TypeReference<>() {});
        return new AiSessionNewResponseDto(
                aiSession.getSessionId(), aiSession.getStatus().name(), aiSession.getTurnCount(), messages);
    }

    private AiSessionGeneratedResponseDto toGeneratedResponse(AiSession aiSession) {
        List<AiGeneratedTaskDto> generatedTasks =
                deserialize(aiSession.getGeneratedTasks(), new TypeReference<>() {});
        return new AiSessionGeneratedResponseDto(
                aiSession.getSessionId(), aiSession.getStatus().name(), generatedTasks);
    }

    // tasks 도메인 미확정 - 확정 후 재검증 필요
    private AiSessionConfirmedResponseDto toConfirmedResponse(AiSession aiSession) {
        List<Long> confirmedTaskIds = parseConfirmedTaskIds(aiSession.getConfirmedTaskIds());
        List<AiConfirmedTaskDto> confirmedTasks =
                confirmedTaskIds.stream()
                        .map(taskId -> new AiConfirmedTaskDto(taskId, null, null, null, null, null, null))
                        .toList();
        return new AiSessionConfirmedResponseDto(
                aiSession.getSessionId(), aiSession.getStatus().name(), confirmedTasks);
    }

    public AiSessionMessageResponseDto sendMessage(String sessionId, String message) {
        validateMessage(message);

        AiSession aiSession =
                aiSessionRepository
                        .findBySessionId(sessionId)
                        .orElseThrow(() -> new CustomException(ErrorCode.AI_SESSION_NOT_FOUND));

        // 확정된 세션은 더 이상 대화를 이어갈 수 없음
        if (aiSession.getStatus() == AiSessionStatus.ALREADY_CONFIRMED) {
            throw new CustomException(ErrorCode.AI_SESSION_ALREADY_CONFIRMED);
        }
        // 이미 일정이 생성된 세션도 대화를 이어가면 status가 NEW로 되돌아가며 생성된 일정이 유실되므로 차단
        if (aiSession.getStatus() == AiSessionStatus.GENERATED) {
            throw new CustomException(ErrorCode.AI_SESSION_TASKS_GENERATED);
        }

        List<AiMessageDto> storedMessages =
                deserialize(aiSession.getMessages(), new TypeReference<>() {});
        List<AiMessageDto> messages = new ArrayList<>(storedMessages);
        messages.add(new AiMessageDto(USER_ROLE, message));

        int nextTurnCount = aiSession.getTurnCount() + 1;
        AiGenerateResponseDto aiResponse = requestGenerate(messages, nextTurnCount);

        if (aiResponse.isFinal()) {
            return saveGeneratedMessage(aiSession, messages, nextTurnCount, aiResponse.tasks());
        }
        return saveNewMessage(aiSession, messages, nextTurnCount, aiResponse.aiMessage());
    }

    private void validateMessage(String message) {
        // 빈 메시지는 AI 서버 호출 전에 차단
        if (message == null || message.isBlank()) {
            throw new CustomException(ErrorCode.AI_EMPTY_MESSAGE);
        }
    }

    private AiSessionMessageResponseDto saveNewMessage(
            AiSession aiSession, List<AiMessageDto> messages, int turnCount, String aiMessage) {
        // AI 서버는 세션을 기억 못 하므로 AI 응답도 이력에 저장
        List<AiMessageDto> updatedMessages = new ArrayList<>(messages);
        updatedMessages.add(new AiMessageDto(AI_ROLE, aiMessage));

        AiSession updatedSession =
                AiSession.builder()
                        .sessionId(aiSession.getSessionId())
                        .status(AiSessionStatus.NEW)
                        .messages(serialize(updatedMessages))
                        .turnCount(turnCount)
                        .generatedTasks(aiSession.getGeneratedTasks())
                        .confirmedTaskIds(aiSession.getConfirmedTaskIds())
                        .version(aiSession.getVersion())
                        .build();
        saveSession(updatedSession);

        return new AiSessionMessageResponseDto(aiMessage, turnCount, false, null);
    }

    private AiSessionMessageResponseDto saveGeneratedMessage(
            AiSession aiSession,
            List<AiMessageDto> messages,
            int turnCount,
            List<AiGenerateTaskDto> tasks) {
        List<AiGeneratedTaskDto> generatedTasks = tasks.stream().map(this::assignTempId).toList();

        AiSession updatedSession =
                AiSession.builder()
                        .sessionId(aiSession.getSessionId())
                        .status(AiSessionStatus.GENERATED)
                        .messages(serialize(messages))
                        .turnCount(turnCount)
                        .generatedTasks(serialize(generatedTasks))
                        .confirmedTaskIds(aiSession.getConfirmedTaskIds())
                        .version(aiSession.getVersion())
                        .build();
        saveSession(updatedSession);

        return new AiSessionMessageResponseDto(null, turnCount, true, generatedTasks);
    }

    public AiGeneratedTaskDto updateTask(
            String sessionId, String tempId, AiSessionTaskUpdateRequestDto request) {
        AiSession aiSession =
                aiSessionRepository
                        .findBySessionId(sessionId)
                        .orElseThrow(() -> new CustomException(ErrorCode.AI_SESSION_NOT_FOUND));

        // 아직 일정이 생성되지 않아 요청한 tempId가 존재할 수 없음
        if (aiSession.getStatus() == AiSessionStatus.NEW) {
            throw new CustomException(ErrorCode.AI_TEMP_TASK_NOT_FOUND);
        }
        // 확정된 세션의 임시 일정은 더 이상 수정할 수 없음
        if (aiSession.getStatus() == AiSessionStatus.ALREADY_CONFIRMED) {
            throw new CustomException(ErrorCode.AI_SESSION_ALREADY_CONFIRMED);
        }

        List<AiGeneratedTaskDto> generatedTasks =
                deserialize(aiSession.getGeneratedTasks(), new TypeReference<>() {});
        generatedTasks.stream()
                .filter(task -> task.tempId().equals(tempId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.AI_TEMP_TASK_NOT_FOUND));

        validateTaskTitle(request.title());

        AiGeneratedTaskDto updatedTask =
                new AiGeneratedTaskDto(
                        tempId,
                        request.title(),
                        request.startDate(),
                        request.startTime(),
                        request.endDate(),
                        request.endTime(),
                        request.memo());
        List<AiGeneratedTaskDto> updatedTasks =
                generatedTasks.stream()
                        .map(task -> task.tempId().equals(tempId) ? updatedTask : task)
                        .toList();

        AiSession updatedSession =
                AiSession.builder()
                        .sessionId(aiSession.getSessionId())
                        .status(AiSessionStatus.GENERATED)
                        .messages(aiSession.getMessages())
                        .turnCount(aiSession.getTurnCount())
                        .generatedTasks(serialize(updatedTasks))
                        .confirmedTaskIds(aiSession.getConfirmedTaskIds())
                        .version(aiSession.getVersion())
                        .build();
        saveSession(updatedSession);

        return updatedTask;
    }

    private void validateTaskTitle(String title) {
        // 빈 제목은 저장 전에 차단
        if (title == null || title.isBlank()) {
            throw new CustomException(ErrorCode.AI_EMPTY_TASK_TITLE);
        }
    }

    private List<Long> parseConfirmedTaskIds(String confirmedTaskIdsJson) {
        // confirm API가 아직 없어 null일 수 있음
        if (confirmedTaskIdsJson == null) {
            return List.of();
        }
        return deserialize(confirmedTaskIdsJson, new TypeReference<>() {});
    }

    private <T> T deserialize(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private AiGeneratedTaskDto assignTempId(AiGenerateTaskDto task) {
        // 아직 Task로 저장 전이라 DB id가 없어 임시 id 부여
        return new AiGeneratedTaskDto(
                UUID.randomUUID().toString(),
                task.title(),
                task.startDate(),
                task.startTime(),
                task.endDate(),
                task.endTime(),
                task.memo());
    }

    private void validateInitialInput(String initialInput) {
        // 빈 입력은 AI 서버 호출 전에 차단
        if (initialInput == null || initialInput.isBlank()) {
            throw new CustomException(ErrorCode.AI_EMPTY_INITIAL_INPUT);
        }
    }

    private AiGenerateResponseDto requestGenerate(List<AiMessageDto> messages, int turnCount) {
        AiGenerateRequestDto requestBody = new AiGenerateRequestDto(messages, turnCount);
        return aiWebClient
                .post()
                .uri(GENERATE_PATH)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleAiServerError)
                .bodyToMono(AiGenerateResponseDto.class)
                .onErrorMap(TimeoutException.class, e -> new CustomException(ErrorCode.AI_LLM_TIMEOUT))
                .block();
    }

    // AI 서버 에러를 도메인 에러로 변환
    private Mono<? extends Throwable> handleAiServerError(ClientResponse response) {
        return response.bodyToMono(AiErrorResponseDto.class)
                .defaultIfEmpty(new AiErrorResponseDto(null))
                .map(error -> new CustomException(mapErrorCode(response.statusCode())));
    }

    private ErrorCode mapErrorCode(HttpStatusCode statusCode) {
        return switch (statusCode.value()) {
            case 424 -> ErrorCode.AI_LLM_GENERATION_FAILED;
            case 504 -> ErrorCode.AI_LLM_TIMEOUT;
            case 503 -> ErrorCode.AI_RAG_SEARCH_FAILED;
            default -> ErrorCode.INTERNAL_SERVER_ERROR;
        };
    }

    // JSON 직렬화 실패 처리를 공용화
    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 동시에 같은 세션에 저장 요청이 들어와 버전이 충돌하면 먼저 저장한 내용이 덮어써지는 대신 에러로 알림
    private void saveSession(AiSession aiSession) {
        try {
            aiSessionRepository.save(aiSession);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new CustomException(ErrorCode.AI_SESSION_CONCURRENT_UPDATE);
        }
    }
}
