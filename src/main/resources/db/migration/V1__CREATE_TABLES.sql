CREATE TABLE IF NOT EXISTS delbestilling
(
    id                 UUID        NOT NULL PRIMARY KEY,
    brukers_kommunenr  varchar(4)  NOT NULL,
    fnr_bruker         varchar(11) NOT NULL,
    fnr_bestiller      varchar(11) NOT NULL,
    delbestilling_json text        NOT NULL,
    opprettet_dato     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);