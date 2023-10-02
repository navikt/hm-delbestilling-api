ALTER TABLE delbestilling ADD COLUMN brukers_kommunenavn VARCHAR(255);

UPDATE delbestilling
SET brukers_kommunenavn = CASE
    WHEN brukers_kommunenr = '5433' THEN 'Hasvik' -- kun for dev
    WHEN brukers_kommunenr = '0301' THEN 'Oslo'
    WHEN brukers_kommunenr = '3034' THEN 'Nes'
    WHEN brukers_kommunenr = '3024' THEN 'Bærum'
    ELSE 'Ukjent'
END;