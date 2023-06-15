CREATE TABLE IF NOT EXISTS delbestilling
(
    saksnummer         BIGSERIAL PRIMARY KEY,
    brukers_kommunenr  varchar(4)  NOT NULL,
    fnr_bruker         varchar(11) NOT NULL,
    fnr_bestiller      varchar(11) NOT NULL,
    delbestilling_json text        NOT NULL,
    opprettet_dato     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);