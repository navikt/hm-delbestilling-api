CREATE TABLE outbox
(
    id         BIGSERIAL    PRIMARY KEY,
    topic      VARCHAR(255) NOT NULL,
    key        VARCHAR(255) NOT NULL,
    event_name VARCHAR(255) NOT NULL,
    event_id   UUID         NOT NULL,
    payload    TEXT         NOT NULL,
    status     VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    attempts   INT          NOT NULL DEFAULT 0,
    last_error TEXT,
    alerted    BOOLEAN      NOT NULL DEFAULT false,
    opprettet  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    publisert  TIMESTAMP
);

CREATE INDEX outbox_status_id_idx ON outbox (status, id);
