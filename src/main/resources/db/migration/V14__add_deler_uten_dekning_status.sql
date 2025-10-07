-- Denne migreringen har desverre et misvisende navn som ikke ble oppdaget før etter at migreringen var prodsatt.
ALTER TABLE deler_uten_dekning
    RENAME COLUMN rapportert_tidspunkt TO behandlet_tidspunkt;