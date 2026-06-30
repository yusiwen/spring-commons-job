ALTER TABLE task_log ADD COLUMN duration_ms BIGINT;
ALTER TABLE task_log ADD COLUMN trigger_type VARCHAR(16);
ALTER TABLE task_log ADD COLUMN task_params CLOB;
ALTER TABLE task_log ADD COLUMN execution_host VARCHAR(128);
