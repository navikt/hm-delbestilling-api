# hm-delbestilling-api

## Testing i dev

### Kalle endepunkt manuelt
F.eks. endepunktene i `DevToolsApi.kt` og `OrdrestatusApi.kt` (simulere statusoppdatering fra OeBS) 
kan være nyttig å kalle manuelt i dev for å teste/verifisere i dev. Her er hvordan man kan koble seg på disse:
- Koble til naisdevice.
- Kjør `nais login` i terminalen.
- Hent token hos [token generator](https://azure-token-generator.intern.dev.nav.no/api/obo?aud=dev-gcp:teamdigihot:hm-delbestilling-api). (Se [Nais-docs](https://docs.nais.io/auth/entra-id/how-to/generate/) for detaljer.)
- Sett opp port-forwarding: `kubectl port-forward deploy/hm-delbestilling-api 8080`
- (Valgfritt) Koble til db dersom du ønsker å sjekke at ting har endret seg: `nais postgres proxy hm-delbestilling-api` ([docs](https://docs.nais.io/operate/cli/reference/postgres/))
- Send request. Eksempel:
```text
curl --request PUT \
  --url http://127.0.0.1:8080/api/delbestilling/status/v2/218 \
  --header 'Authorization: Bearer ey...mQ' \
  --header 'Content-Type: application/json' \
  --data '{
	"status": "ANNULLERT",
	"oebsOrdrenummer": "9707250"
}'
```

### Testverdier for utlån
```
artnr: 250042
serienr: 730303
fnr: 26848497710
```

Dersom verdiene over er utdatert, så kan du:
- port-forward til podden i dev
- gjør GET mot `/api/finnGyldigTestbruker`
- vente et par minutt, så vil du få ut fnr og artnr som kan brukes

## Legge inn nye deler
- Last ned "Deler som skal legges inn"-excelfilen. 
- Legg den i `resources/` med navn "deler.xlsx".
- Kjør `ManglendeProdukt.main()`
- Kopier fra konsoll-output til relevante filer i `hjelpemidler/data/`
- Legg inn manglende verdier på deler (default-/maksantall, kategori, ...)
- Kjør `Datavalidering.main()` for å kontrollere at deler, hjelpemiddel og koblinger som er i bruk også er definert. 