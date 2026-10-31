CREATE TABLE IF NOT EXISTS transactions.outbox_events
(
    uuid         UUID                     NOT NULL PRIMARY KEY ,
    user_uuid UUID                     NOT NULL,
    topic        VARCHAR(128)             NOT NULL,
    payload      JSONB                    NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    processed    BOOLEAN                  NOT NULL DEFAULT FALSE,
    trace_id VARCHAR(64),
    transaction_id UUID NOT NULL
);


CREATE INDEX idx_outbox_events_processed_false
    ON transactions.outbox_events (created_at) WHERE processed = FALSE;