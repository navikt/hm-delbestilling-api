ALTER TABLE deler_uten_dekning
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'AVVENTER';

UPDATE deler_uten_dekning
SET status = 'BEHANDLET'
WHERE behandlet_tidspunkt IS NOT NULL;

ALTER TABLE deler_uten_dekning
    ALTER COLUMN status DROP DEFAULT;