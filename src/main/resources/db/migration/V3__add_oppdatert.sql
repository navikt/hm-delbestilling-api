ALTER TABLE delbestilling ADD COLUMN sist_oppdatert TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
UPDATE delbestilling SET sist_oppdatert = opprettet;