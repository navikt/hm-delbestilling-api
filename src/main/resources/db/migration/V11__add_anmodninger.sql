CREATE TABLE IF NOT EXISTS anmodninger
(
    id              BIGSERIAL PRIMARY KEY,
    enhetnr         VARCHAR(4)   NOT NULL,
    hmsnr           VARCHAR(6)   NOT NULL,
    navn            VARCHAR(511) NOT NULL,
    antall_anmodet  SMALLINT     NOT NULL,
    antall_på_lager SMALLINT     NOT NULL,
    opprettet       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sist_oppdatert  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

