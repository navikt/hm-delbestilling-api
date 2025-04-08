CREATE TABLE IF NOT EXISTS deler_uten_dekning
(
    id                  BIGSERIAL PRIMARY KEY,
    saksnummer          BIGINT REFERENCES delbestilling (saksnummer),
    hmsnr               VARCHAR(6)   NOT NULL,
    navn                VARCHAR(511) NOT NULL,
    antall_uten_dekning SMALLINT     NOT NULL,
    brukers_kommunenr   VARCHAR(4)   NOT NULL,
    brukers_kommunenavn VARCHAR(255) NOT NULL,
    enhetnr             VARCHAR(4)   NOT NULL,
    status              VARCHAR(30)  NOT NULL,
    opprettet           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sist_oppdatert      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

