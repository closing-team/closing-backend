package com.closing.closing.domain.ai.controller;

import com.closing.closing.domain.ai.dto.request.AiSessionMessageRequestDto;
import com.closing.closing.domain.ai.dto.request.AiSessionRequestDto;
import com.closing.closing.domain.ai.dto.request.AiSessionTaskUpdateRequestDto;
import com.closing.closing.domain.ai.dto.response.AiGeneratedTaskDto;
import com.closing.closing.domain.ai.dto.response.AiSessionConfirmedResponseDto;
import com.closing.closing.domain.ai.dto.response.AiSessionDetailResponseDto;
import com.closing.closing.domain.ai.dto.response.AiSessionMessageResponseDto;
import com.closing.closing.domain.ai.dto.response.AiSessionResponseDto;
import com.closing.closing.domain.ai.service.AiSessionService;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI", description = "AI 세션 API")
@RestController
@RequestMapping("/api/v1/ai/sessions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AiSessionController {

    private final AiSessionService aiSessionService;

    //세션 시작
    @Operation(
            summary = "AI 세션 시작",
            description = """
                    초기 상황 설명을 전달해 AI 세션을 시작합니다.
                    이미 확정된 세션이 있으면 새로 만들지 않고 기존 세션 정보를 반환합니다.
                    """)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", description = "AI 세션 시작 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "초기 상황 입력 내용 없음",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "초기 입력 없음",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "AI_INITIAL_INPUT400",
                                                          "message": "초기 상황 입력 내용이 없습니다."
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 토큰이 없거나 만료됨",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "인증 실패",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "COMMON401",
                                                          "message": "인증이 필요합니다."
                                                        }
                                                        """)))
    })
    @PostMapping
    public ApiResponse<AiSessionResponseDto> createSession(
            @AuthenticationPrincipal Long userId, @RequestBody AiSessionRequestDto request) {
        return ApiResponse.onSuccess(aiSessionService.createSession(userId, request));
    }

    //세션 조회
    @Operation(summary = "AI 세션 조회", description = "세션 ID로 AI 세션의 상세 정보를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", description = "AI 세션 조회 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 토큰이 없거나 만료됨",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "인증 실패",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "COMMON401",
                                                          "message": "인증이 필요합니다."
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "본인의 세션이 아님",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "세션 접근 권한 없음",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "AI_SESSION_ACCESS_FORBIDDEN",
                                                          "message": "본인의 세션만 접근할 수 있습니다."
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "세션을 찾을 수 없음",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "세션 없음",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "AI_SESSION404",
                                                          "message": "존재하지 않는 세션입니다."
                                                        }
                                                        """)))
    })
    @GetMapping("/{sessionId}")
    public ApiResponse<AiSessionDetailResponseDto> getSession(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "조회할 세션 ID", required = true) @PathVariable String sessionId) {
        return ApiResponse.onSuccess(aiSessionService.getSession(userId, sessionId));
    }

    //메시지 전송
    @Operation(
            summary = "AI 세션 메시지 전송",
            description = "세션에 이어서 사용자 메시지를 전달하고 AI의 다음 응답을 받습니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", description = "AI 세션 메시지 전송 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "대화 메시지 내용 없음",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "메시지 없음",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "AI_MESSAGE400",
                                                          "message": "대화 메시지 내용이 없습니다."
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 토큰이 없거나 만료됨",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "인증 실패",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "COMMON401",
                                                          "message": "인증이 필요합니다."
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "본인의 세션이 아님",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "세션 접근 권한 없음",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "AI_SESSION_ACCESS_FORBIDDEN",
                                                          "message": "본인의 세션만 접근할 수 있습니다."
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "세션을 찾을 수 없음",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "세션 없음",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "AI_SESSION404",
                                                          "message": "존재하지 않는 세션입니다."
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "더 이상 대화를 진행할 수 없는 세션",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples = {
                                    @ExampleObject(
                                            name = "이미 확정된 세션",
                                            value =
                                            """
                                            {
                                              "success": false,
                                              "code": "AI_SESSION409",
                                              "message": "이미 확정되어 더 이상 진행할 수 없는 세션입니다."
                                            }
                                            """),
                                    @ExampleObject(
                                            name = "이미 일정이 생성된 세션",
                                            value =
                                            """
                                            {
                                              "success": false,
                                              "code": "AI_SESSION_TASKS409",
                                              "message": "이미 일정이 생성되어 더 이상 대화를 진행할 수 없는 세션입니다."
                                            }
                                            """)
                                }))
    })
    @PostMapping("/{sessionId}/messages")
    public ApiResponse<AiSessionMessageResponseDto> sendMessage(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "메시지를 전송할 세션 ID", required = true) @PathVariable
                    String sessionId,
            @RequestBody AiSessionMessageRequestDto request) {
        return ApiResponse.onSuccess(
                aiSessionService.sendMessage(userId, sessionId, request.message()));
    }

    //임시 일정 수정
    @Operation(summary = "AI 생성 임시 일정 수정", description = "AI가 생성한 임시 일정 하나를 수정합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "AI 생성 임시 일정 수정 성공",
                useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "일정 제목이 비어있음",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "제목 없음",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "AI_TASK_TITLE400",
                                                          "message": "일정 제목이 비어있습니다."
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 토큰이 없거나 만료됨",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "인증 실패",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "COMMON401",
                                                          "message": "인증이 필요합니다."
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "본인의 세션이 아님",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "세션 접근 권한 없음",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "AI_SESSION_ACCESS_FORBIDDEN",
                                                          "message": "본인의 세션만 접근할 수 있습니다."
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "세션 또는 임시 일정을 찾을 수 없음",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples = {
                                    @ExampleObject(
                                            name = "세션 없음",
                                            value =
                                                    """
                                                    {
                                                      "success": false,
                                                      "code": "AI_SESSION404",
                                                      "message": "존재하지 않는 세션입니다."
                                                    }
                                                    """),
                                    @ExampleObject(
                                            name = "임시 일정 없음",
                                            value =
                                                    """
                                                    {
                                                      "success": false,
                                                      "code": "AI_TEMP_TASK404",
                                                      "message": "존재하지 않는 임시 일정입니다."
                                                    }
                                                    """)
                                })),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "이미 확정되어 수정할 수 없는 세션",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "이미 확정된 세션",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "AI_SESSION409",
                                                          "message": "이미 확정되어 더 이상 진행할 수 없는 세션입니다."
                                                        }
                                                        """)))
    })
    @PatchMapping("/{sessionId}/tasks/{tempId}")
    public ApiResponse<AiGeneratedTaskDto> updateTask(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "임시 일정이 속한 세션 ID", required = true) @PathVariable
                    String sessionId,
            @Parameter(description = "수정할 임시 일정 ID", example = "task-1", required = true)
                    @PathVariable
                    String tempId,
            @RequestBody AiSessionTaskUpdateRequestDto request) {
        return ApiResponse.onSuccess(
                aiSessionService.updateTask(userId, sessionId, tempId, request));
    }

    //임시 일정 삭제
    @Operation(summary = "AI 생성 임시 일정 삭제", description = "AI가 생성한 임시 일정 하나를 삭제합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "AI 생성 임시 일정 삭제 성공",
                useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 토큰이 없거나 만료됨",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "인증 실패",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "COMMON401",
                                                          "message": "인증이 필요합니다."
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "본인의 세션이 아님",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "세션 접근 권한 없음",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "AI_SESSION_ACCESS_FORBIDDEN",
                                                          "message": "본인의 세션만 접근할 수 있습니다."
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "세션 또는 임시 일정을 찾을 수 없음",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples = {
                                    @ExampleObject(
                                            name = "세션 없음",
                                            value =
                                                    """
                                                    {
                                                      "success": false,
                                                      "code": "AI_SESSION404",
                                                      "message": "존재하지 않는 세션입니다."
                                                    }
                                                    """),
                                    @ExampleObject(
                                            name = "임시 일정 없음",
                                            value =
                                                    """
                                                    {
                                                      "success": false,
                                                      "code": "AI_TEMP_TASK404",
                                                      "message": "존재하지 않는 임시 일정입니다."
                                                    }
                                                    """)
                                })),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "이미 확정되어 삭제할 수 없는 세션",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "이미 확정된 세션",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "AI_SESSION409",
                                                          "message": "이미 확정되어 더 이상 진행할 수 없는 세션입니다."
                                                        }
                                                        """)))
    })
    @DeleteMapping("/{sessionId}/tasks/{tempId}")
    public ApiResponse<Void> deleteTask(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "임시 일정이 속한 세션 ID", required = true) @PathVariable
                    String sessionId,
            @Parameter(description = "삭제할 임시 일정 ID", example = "task-1", required = true)
                    @PathVariable
                    String tempId) {
        aiSessionService.deleteTask(userId, sessionId, tempId);
        return ApiResponse.onSuccess(null);
    }

    //세션 확정
    @Operation(
            summary = "AI 세션 확정 일정 캘린더 반영",
            description = "생성된 임시 일정을 실제 캘린더(tasks)에 확정 반영합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", description = "AI 세션 확정 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 토큰이 없거나 만료됨",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "인증 실패",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "COMMON401",
                                                          "message": "인증이 필요합니다."
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "본인의 세션이 아님",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "세션 접근 권한 없음",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "AI_SESSION_ACCESS_FORBIDDEN",
                                                          "message": "본인의 세션만 접근할 수 있습니다."
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "세션을 찾을 수 없음",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "세션 없음",
                                                value =
                                                        """
                                                        {
                                                          "success": false,
                                                          "code": "AI_SESSION404",
                                                          "message": "존재하지 않는 세션입니다."
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "확정할 수 없는 세션",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiResponse.class),
                                examples = {
                                    @ExampleObject(
                                            name = "이미 확정된 세션",
                                            value =
                                            """
                                            {
                                              "success": false,
                                              "code": "AI_SESSION409",
                                              "message": "이미 확정되어 더 이상 진행할 수 없는 세션입니다."
                                            }
                                            """),
                                    @ExampleObject(
                                            name = "확정할 일정 없음",
                                            value =
                                            """
                                            {
                                              "success": false,
                                              "code": "AI_NO_TASKS409",
                                              "message": "확정할 일정이 없습니다."
                                            }
                                            """)
                                }))
    })
    @PostMapping("/{sessionId}/confirm")
    public ApiResponse<AiSessionConfirmedResponseDto> confirmSession(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "확정할 세션 ID", required = true) @PathVariable String sessionId) {
        return ApiResponse.onSuccess(aiSessionService.confirmSession(userId, sessionId));
    }
}
