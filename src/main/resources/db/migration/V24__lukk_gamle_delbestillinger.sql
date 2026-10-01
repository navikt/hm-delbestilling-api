/*
 * Sak 36 og 38 er gamle saker (fra 4. og 5. september 2023) som aldri fikk ordrenummer fra OeBS (ble implementert skikkelig senere?).
 * Dermed har de blitt liggende som KLARGJORT og komt med i varsel til Hms Oslo om gamle saker som ikke har blitt
 * lukket. Dette er bare støy. Vi kan anta at disse sakene i realiteten er LUKKET, og overskriver derfor status
 * manuelt her.
 */
UPDATE delbestilling
SET status = 'LUKKET'
WHERE saksnummer IN (36, 38)
  AND status = 'KLARGJORT';
