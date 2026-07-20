package com.closing.closing.domain.ai.repository;

import com.closing.closing.domain.ai.entity.AiSession;
import java.util.Optional;

// 세션 저장 위치: 현재 PostgreSQL 사용 중 (추후 필요 시 Redis 전환 검토)
// 현재는 JPA(DB) 구현체만 존재
public interface AiSessionRepository {

    AiSession save(AiSession aiSession);

    Optional<AiSession> findBySessionId(String sessionId);
}
