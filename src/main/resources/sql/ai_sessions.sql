-- Redis 등으로 전환이 결정되면 이 테이블은 제거될 수 있습니다.
CREATE TABLE IF NOT EXISTS ai_sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL,
    messages TEXT NOT NULL,
    turn_count INTEGER NOT NULL,
    generated_tasks TEXT,
    confirmed_task_ids TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 기존 테이블에 confirmed_task_ids 컬럼 추가용 마이그레이션
ALTER TABLE ai_sessions ADD COLUMN IF NOT EXISTS confirmed_task_ids TEXT;

-- 기존 테이블에 낙관적 잠금용 version 컬럼 추가용 마이그레이션
ALTER TABLE ai_sessions ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- 기존 테이블에 소유자 식별용 user_id 컬럼 추가용 마이그레이션
-- 기존 행에는 소유자를 알 수 없어 임시로 0을 채움 (실제 운영 데이터가 있다면 별도 백필 필요)
ALTER TABLE ai_sessions ADD COLUMN IF NOT EXISTS user_id BIGINT NOT NULL DEFAULT 0;
