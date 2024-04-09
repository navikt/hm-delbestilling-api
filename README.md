# hm-delbestilling-api

## Testing i dev

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