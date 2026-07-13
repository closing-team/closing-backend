package com.closing.closing.domain.ai.repository;

import com.closing.closing.domain.ai.entity.AiSession;
import java.util.Optional;

// 세션 저장 위치(DB vs Redis)는 팀 논의 중 - 구현체 교체가 쉽도록 인터페이스로 분리
// 현재는 JPA(DB) 구현체(AiSessionRepositoryImpl)만 존재
public interface AiSessionRepository {

    AiSession save(AiSession aiSession);

    Optional<AiSession> findBySessionId(String sessionId);
}
