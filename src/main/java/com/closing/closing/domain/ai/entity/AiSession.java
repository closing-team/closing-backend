package com.closing.closing.domain.ai.entity;

import com.closing.closing.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiSessionStatus status;

    // AiMessageDto 리스트를 JSON 직렬화한 문자열 (대화 이력 전체)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String messages;

    @Column(nullable = false)
    private int turnCount;

    @Builder
    public AiSession(String sessionId, AiSessionStatus status, String messages, int turnCount) {
        this.sessionId = sessionId;
        this.status = status;
        this.messages = messages;
        this.turnCount = turnCount;
    }
}
