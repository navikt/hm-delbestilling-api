CREATE TABLE bestillerepost
(
    fnr_bestiller VARCHAR(11) PRIMARY KEY,
    epost         VARCHAR(255) NOT NULL,
    oppdatert     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
