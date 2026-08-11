ALTER TABLE tasks
    ADD COLUMN user_id BIGINT;

UPDATE tasks
SET user_id = (
    SELECT business_registrations.user_id
    FROM business_registrations
    WHERE business_registrations.registration_id = tasks.registration_id
);

ALTER TABLE tasks
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id) REFERENCES users (user_id);

CREATE INDEX idx_tasks_user
    ON tasks (user_id);

ALTER TABLE tasks
    DROP CONSTRAINT fk_tasks_registration;

ALTER TABLE tasks
    DROP COLUMN registration_id;
