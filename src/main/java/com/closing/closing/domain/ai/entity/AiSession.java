package com.closing.closing.domain.ai.entity;

import com.closing.closing.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "ai_sessions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiSession extends BaseEntity {

    @Id
    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiSessionStatus status;

    // AiMessageDto 리스트를 JSON 직렬화한 문자열 (대화 이력 전체)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String messages;

    @Column(nullable = false)
    private int turnCount;

    // AiGeneratedTaskDto 리스트를 JSON 직렬화한 문자열 (status가 GENERATED일 때만 존재)
    @Column(columnDefinition = "TEXT")
    private String generatedTasks;

    // Long(taskId) 리스트를 JSON 직렬화한 문자열 (status가 ALREADY_CONFIRMED일 때만 존재)
    // 값을 채우는 confirm API는 아직 없음 - 해당 이슈에서 저장 로직 추가 예정
    @Column(columnDefinition = "TEXT")
    private String confirmedTaskIds;

    // 동시 요청으로 인한 덮어쓰기 방지 (낙관적 잠금) - 새 세션 생성 시엔 null로 두고, 기존 세션
    // 갱신 시엔 조회했던 값을 그대로 넘겨야 Hibernate가 충돌을 감지할 수 있음
    @Version
    private Long version;

    @Builder
    public AiSession(
            String sessionId,
            Long userId,
            AiSessionStatus status,
            String messages,
            int turnCount,
            String generatedTasks,
            String confirmedTaskIds,
            Long version) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.status = status;
        this.messages = messages;
        this.turnCount = turnCount;
        this.generatedTasks = generatedTasks;
        this.confirmedTaskIds = confirmedTaskIds;
        this.version = version;
    }
}
