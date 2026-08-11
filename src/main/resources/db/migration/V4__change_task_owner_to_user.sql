DELETE FROM ai_sessions
WHERE status = 'ALREADY_CONFIRMED'
   OR confirmed_task_ids IS NOT NULL;

DELETE FROM tasks;

ALTER TABLE tasks
    DROP CONSTRAINT fk_tasks_registration;

ALTER TABLE tasks
    DROP COLUMN registration_id;

ALTER TABLE tasks
    ADD COLUMN user_id BIGINT NOT NULL;

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id) REFERENCES users (user_id);

CREATE INDEX idx_tasks_user
    ON tasks (user_id);
