package com.closing.closing.domain.ai.repository.jpa;

import com.closing.closing.domain.ai.entity.AiSession;
import com.closing.closing.domain.ai.entity.AiSessionStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiSessionJpaRepository extends JpaRepository<AiSession, String> {

    Optional<AiSession> findByUserIdAndStatus(Long userId, AiSessionStatus status);
}
