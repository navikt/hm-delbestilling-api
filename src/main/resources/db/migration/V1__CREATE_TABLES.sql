CREATE TABLE IF NOT EXISTS delbestilling
(
    id                 UUID        NOT NULL PRIMARY KEY,
    hmsnr              varchar(6)  NOT NULL,
    serienr            varchar(6)  NOT NULL,
    fnr_bruker         varchar(11) NOT NULL,
    fnr_innsender      varchar(11) NOT NULL,
    delbestilling_json text        NOT NULL,
    opprettet_dato     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
