# hm-delbestilling-api

## Testing i dev

### Testverdier for utlån
```json
{
  "utlån": {
    "fnr": "26848497710",
    "artnr": "236958",
    "serienr": "687273",
    "utlånsDato": "2023-06-01 14:00:57"
  }
}
```

Dersom verdiene over er utdatert, så kan du:
- port-forward til podden i dev
- gjør GET mot `/api/finnGyldigTestbruker`
- vente et par minutt, så vil du få ut fnr og artnr som kan brukes 