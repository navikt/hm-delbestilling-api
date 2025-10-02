-- Triggerfunksjon for å oppdatere timestamp i sist_oppdatert-kollone
CREATE OR REPLACE FUNCTION update_sist_oppdatert_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.sist_oppdatert := CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- Opprett trigger for delbestilling
CREATE TRIGGER set_sist_oppdatert_delbestilling
    BEFORE UPDATE ON delbestilling
    FOR EACH ROW
EXECUTE FUNCTION update_sist_oppdatert_column();

-- Opprett trigger for deler_uten_dekning
CREATE TRIGGER set_sist_oppdatert_deler_uten_dekning
    BEFORE UPDATE ON deler_uten_dekning
    FOR EACH ROW
EXECUTE FUNCTION update_sist_oppdatert_column();

-- Opprett trigger for anmodninger
CREATE TRIGGER set_sist_oppdatert_anmodninger
    BEFORE UPDATE ON anmodninger
    FOR EACH ROW
EXECUTE FUNCTION update_sist_oppdatert_column();