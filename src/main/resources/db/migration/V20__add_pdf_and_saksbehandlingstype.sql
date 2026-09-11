ALTER TABLE delbestilling ADD COLUMN pdf BYTEA DEFAULT NULL;
ALTER TABLE delbestilling ADD COLUMN saksbehandlingstype varchar(20) NOT NULL DEFAULT 'AUTOMATISK';