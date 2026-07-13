-- NOTE: 세션 데이터 저장 위치(DB vs Redis)는 팀 논의 중인 임시 구현입니다.
-- Redis 등으로 전환이 결정되면 이 테이블은 제거될 수 있습니다.
-- (참고: domain/ai/repository/AiSessionRepository.java)
CREATE TABLE IF NOT EXISTS ai_sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    messages TEXT NOT NULL,
    turn_count INTEGER NOT NULL,
    generated_tasks TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
