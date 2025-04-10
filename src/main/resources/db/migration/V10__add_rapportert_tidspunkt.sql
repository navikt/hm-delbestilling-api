ALTER TABLE deler_uten_dekning
    ADD COLUMN rapportert_tidspunkt TIMESTAMP DEFAULT NULL;

ALTER TABLE deler_uten_dekning
    DROP COLUMN IF EXISTS status;