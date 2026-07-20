-- Redis 등으로 전환이 결정되면 이 테이블은 제거될 수 있습니다.
CREATE TABLE IF NOT EXISTS ai_sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    messages TEXT NOT NULL,
    turn_count INTEGER NOT NULL,
    generated_tasks TEXT,
    confirmed_task_ids TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
