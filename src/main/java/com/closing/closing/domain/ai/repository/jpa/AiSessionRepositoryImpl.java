package com.closing.closing.domain.ai.repository.jpa;

import com.closing.closing.domain.ai.entity.AiSession;
import com.closing.closing.domain.ai.repository.AiSessionRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AiSessionRepositoryImpl implements AiSessionRepository {

    private final AiSessionJpaRepository aiSessionJpaRepository;

    @Override
    public AiSession save(AiSession aiSession) {
        return aiSessionJpaRepository.save(aiSession);
    }

    @Override
    public Optional<AiSession> findBySessionId(String sessionId) {
        return aiSessionJpaRepository.findById(sessionId);
    }
}
