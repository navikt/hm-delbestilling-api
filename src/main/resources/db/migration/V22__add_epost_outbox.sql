CREATE TABLE epost_outbox
(
    id             BIGSERIAL    PRIMARY KEY,
    mottaker       VARCHAR(255) NOT NULL,
    emne           VARCHAR(255) NOT NULL,
    html           TEXT         NOT NULL,
    status         VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    attempts       INT          NOT NULL DEFAULT 0,
    last_error     TEXT,
    alerted        BOOLEAN      NOT NULL DEFAULT false,
    opprettet      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sendt          TIMESTAMP
);

CREATE INDEX epost_outbox_status_id_idx ON epost_outbox (status, id);