package com.closing.closing.domain.ai.repository.jpa;

import com.closing.closing.domain.ai.entity.AiSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiSessionJpaRepository extends JpaRepository<AiSession, String> {}
