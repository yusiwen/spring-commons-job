CREATE TABLE IF NOT EXISTS task_info (
    id              VARCHAR(64)   PRIMARY KEY,
    name            VARCHAR(128)  NOT NULL,
    cron_expression VARCHAR(64)   NOT NULL,
    bean_name       VARCHAR(128)  NOT NULL,
    method_name     VARCHAR(128)  NOT NULL,
    params          CLOB,
    status          VARCHAR(16)   NOT NULL DEFAULT 'ENABLED',
    execute_mode    VARCHAR(16)   NOT NULL DEFAULT 'LOCAL',
    callback_url    VARCHAR(512),
    concurrent      BOOLEAN       NOT NULL DEFAULT FALSE,
    last_trigger_at TIMESTAMP,
    last_end_at     TIMESTAMP,
    last_result     VARCHAR(16),
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS task_log (
    id              VARCHAR(64)   PRIMARY KEY,
    task_id         VARCHAR(64)   NOT NULL,
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,
    duration_ms     BIGINT,
    trigger_type    VARCHAR(16),
    task_params     CLOB,
    execution_host  VARCHAR(128),
    result          VARCHAR(16),
    error_message   CLOB,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
