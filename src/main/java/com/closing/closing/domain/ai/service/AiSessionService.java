package com.closing.closing.domain.ai.service;

import com.closing.closing.domain.ai.dto.AiErrorResponseDto;
import com.closing.closing.domain.ai.dto.AiGenerateRequestDto;
import com.closing.closing.domain.ai.dto.AiGenerateResponseDto;
import com.closing.closing.domain.ai.dto.AiMessageDto;
import com.closing.closing.domain.ai.dto.AiSessionRequestDto;
import com.closing.closing.domain.ai.dto.AiSessionResponseDto;
import com.closing.closing.domain.ai.entity.AiSession;
import com.closing.closing.domain.ai.entity.AiSessionStatus;
import com.closing.closing.domain.ai.repository.AiSessionRepository;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AiSessionService {

    private static final String GENERATE_PATH = "/api/ai/generate";
    private static final String USER_ROLE = "user";
    private static final int INITIAL_TURN_COUNT = 1;

    private final WebClient aiWebClient;
    private final AiSessionRepository aiSessionRepository;
    private final ObjectMapper objectMapper;

    public AiSessionResponseDto createSession(AiSessionRequestDto request) {
        validateInitialInput(request.initialInput());

        List<AiMessageDto> messages = List.of(new AiMessageDto(USER_ROLE, request.initialInput()));
        AiGenerateResponseDto aiResponse = requestGenerate(messages, INITIAL_TURN_COUNT);

        String sessionId = UUID.randomUUID().toString();
        AiSession aiSession =
                AiSession.builder()
                        .sessionId(sessionId)
                        .status(AiSessionStatus.NEW)
                        .messages(serializeMessages(messages))
                        .turnCount(INITIAL_TURN_COUNT)
                        .build();
        aiSessionRepository.save(aiSession);

        return new AiSessionResponseDto(
                sessionId, AiSessionStatus.NEW.name(), aiResponse.aiMessage(), INITIAL_TURN_COUNT);
    }

    private void validateInitialInput(String initialInput) {
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

    private String serializeMessages(List<AiMessageDto> messages) {
        try {
            return objectMapper.writeValueAsString(messages);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
