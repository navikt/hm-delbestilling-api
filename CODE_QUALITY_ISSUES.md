# Specific Code Quality Issues & Fixes

This document provides specific issues found in the codebase with recommended fixes.

## Critical Issues

### 1. Duplicate Exception Handler (ErrorHandling.kt)

**File:** `src/main/kotlin/no/nav/hjelpemidler/delbestilling/config/ErrorHandling.kt`  
**Lines:** 32-37

**Issue:**
```kotlin
exception<PdlResponseMissingData> { call, cause ->
    call.respond(HttpStatusCode.InternalServerError, cause.message.orUnknown())
}
exception<PdlResponseMissingData> { call, cause ->  // DUPLICATE!
    call.respond(HttpStatusCode.Forbidden, cause.message.orUnknown())
}
```

**Impact:** The second handler overrides the first. Only one will be used.

**Recommended Fix:**
Determine the correct status code based on the context. If both cases are valid, create separate exception types:
```kotlin
sealed class PdlException(message: String) : RuntimeException(message)
class PdlResponseMissingData(message: String) : PdlException(message)
class PdlAccessDenied(message: String) : PdlException(message)

// Then handle separately:
exception<PdlResponseMissingData> { call, cause ->
    call.respond(HttpStatusCode.InternalServerError, cause.message.orUnknown())
}
exception<PdlAccessDenied> { call, cause ->
    call.respond(HttpStatusCode.Forbidden, cause.message.orUnknown())
}
```

## High Priority Issues

### 2. Environment-Specific Business Logic (DelbestillingService.kt)

**File:** `src/main/kotlin/no/nav/hjelpemidler/delbestilling/delbestilling/DelbestillingService.kt`

**Issue 1 - Self-ordering check (line 85):**
```kotlin
// Det skal ikke være mulig å bestille til seg selv (disabler i dev pga testdata)
if (isProd() && bestillerFnr == brukersFnr) {
    log.info { "Bestiller prøver å bestille til seg selv" }
    return DelbestillingResultat(id, feil = DelbestillingFeil.BESTILLE_TIL_SEG_SELV)
}
```

**Impact:** Business rule only applies in production. Makes testing unreliable.

**Recommended Fix:**
```kotlin
// Create configuration
data class ValidationConfig(
    val allowSelfOrdering: Boolean = false,
    val validateAddressMatch: Boolean = true,
    val enableRateLimiting: Boolean = true
)

// In AppContext or config:
val validationConfig = ValidationConfig(
    allowSelfOrdering = isDev(),
    validateAddressMatch = !isDev(),
    enableRateLimiting = !isDev()
)

// In service:
if (!validationConfig.allowSelfOrdering && bestillerFnr == brukersFnr) {
    log.info { "Bestiller prøver å bestille til seg selv" }
    return DelbestillingResultat(id, feil = DelbestillingFeil.BESTILLE_TIL_SEG_SELV)
}
```

**Issue 2 - Address validation (line 93):**
```kotlin
if (!isDev() && !brukerHarSammeKommunenrIOebsOgPdl) {
    log.info { "Ulik leveringsadresse. OEBS: $oebsBrukerinfo, PDL: $brukerKommunenr" }
    return DelbestillingResultat(id, feil = DelbestillingFeil.ULIK_ADRESSE_PDL_OEBS)
}
```

**Recommended Fix:** Use `validationConfig.validateAddressMatch`

**Issue 3 - Test data workaround (line 106):**
```kotlin
if (isDev()) {
    innsendersRepresenterteOrganisasjon = Organisasjon("1234", navn = "Testorg for dev")
}
```

**Impact:** Production code contains test-specific workarounds.

**Recommended Fix:**
- Remove from production code
- Use test fixtures in test code
- Or use environment-specific test data service

**Issue 4 - Rate limiting (line 189):**
```kotlin
if (isDev()) {
    return null // For enklere testing i dev
}
```

**Recommended Fix:** Use `validationConfig.enableRateLimiting`

### 3. Large Data Files

**Files:**
- `src/main/kotlin/no/nav/hjelpemidler/delbestilling/oppslag/legacy/data/Deler.kt` (1,397 lines)
- `src/main/kotlin/no/nav/hjelpemidler/delbestilling/oppslag/legacy/data/Hjelpemidler.kt` (417 lines)
- `src/main/kotlin/no/nav/hjelpemidler/delbestilling/oppslag/legacy/data/DelerPerHjelpemiddel.kt` (406 lines)

**Issue:** Data hardcoded in Kotlin files makes maintenance difficult.

**Recommended Approach:**

1. **Extract to JSON files:**
```kotlin
// resources/data/deler.json
[
  {
    "hmsnr": "022005",
    "navn": "Batteri 80A inkl poler",
    "kategori": "Batteri",
    "defaultAntall": 2,
    "maksAntall": 2
  },
  ...
]
```

2. **Load at startup:**
```kotlin
class DelerRepository {
    private val deler: Map<Hmsnr, Del> by lazy {
        val json = this::class.java.getResourceAsStream("/data/deler.json")
            ?.readBytes()
            ?.let { String(it) }
            ?: throw IllegalStateException("Could not load deler.json")
        
        jsonMapper.readValue<List<Del>>(json)
            .associateBy { it.hmsnr }
    }
    
    fun findByHmsnr(hmsnr: Hmsnr): Del? = deler[hmsnr]
    fun getAll(): Collection<Del> = deler.values
}
```

3. **Benefits:**
   - Easier to update without code changes
   - Can be externalized for different environments
   - Reduces code size
   - Can be validated separately
   - Easier to import/export

### 4. TODO Comments Without Issues

**Found 8 TODOs in production code:**

| File | Line | TODO |
|------|------|------|
| DelbestillingService.kt | 118 | rydd og splitt ut logikk i egne klasser |
| FinnLagerenhet.kt | 11 | Logikk bør flyttes til felles plass |
| FinnLagerenhet.kt | 53 | Sjekk om Agder må håndteres |
| Oebs.kt | 42 | endre all bruk til AsMap variant |
| Norg.kt | 12 | Kan godt ta i bruk cache |
| Dto.kt | 55 | heller bruk enum |
| DelbestillingStatusService.kt | 92 | Denne kan fjernes på sikt |
| Deler.kt | 8 | Bestem max antall |

**Recommended Actions:**

1. **Create GitHub issues for each TODO**
2. **High priority:**
   - Norg.kt: Implement caching (performance impact)
   - Oebs.kt: Refactor to use AsMap variant (consistency)
   - Dto.kt: Use enum instead of string (type safety)

3. **Medium priority:**
   - DelbestillingService.kt: Refactor into smaller classes
   - FinnLagerenhet.kt: Centralize mapping logic

4. **Low priority:**
   - Deler.kt: Determine appropriate max values
   - DelbestillingStatusService.kt: Remove when safe

## Medium Priority Issues

### 5. Missing Caching Implementation (Norg.kt)

**File:** `src/main/kotlin/no/nav/hjelpemidler/delbestilling/infrastructure/norg/Norg.kt`  
**Line:** 12

**Current:**
```kotlin
class Norg(private val client: NorgClient) {
    suspend fun hentEnhet(enhetNr: String): NorgEnhet {
        // TODO Denne kan godt ta i bruk cache
        return client.hentEnhet(enhetNr)
    }
}
```

**Recommended Fix:**
```kotlin
class Norg(
    private val client: NorgClient,
    private val cache: LoadingCache<String, NorgEnhet> = Caffeine.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build()
) {
    suspend fun hentEnhet(enhetNr: String): NorgEnhet {
        return cache.get(enhetNr) {
            runBlocking { client.hentEnhet(enhetNr) }
        }
    }
}
```

**Note:** Caffeine cache is already a dependency in the project.

### 6. Magic Numbers and Constants

**Issue:** Business rules as magic numbers

**Examples:**
```kotlin
// DelbestillingService.kt
val maxAntallBestillingerPer24Timer = 5

// Deler.kt
private const val TODO_BESTEM_MAX_ANTALL = 8
```

**Recommended Fix:**
Create a configuration object:

```kotlin
// config/BusinessRules.kt
data class BusinessRules(
    val maxOrdersPer24Hours: Int = 5,
    val defaultMaxPartQuantity: Int = 8,
    val orderHistoryDays: Int = 1,
    val shutdownTimeoutSeconds: Long = 10
) {
    companion object {
        fun fromEnvironment(): BusinessRules {
            return BusinessRules(
                maxOrdersPer24Hours = env("MAX_ORDERS_PER_24H")?.toInt() ?: 5,
                defaultMaxPartQuantity = env("DEFAULT_MAX_PART_QTY")?.toInt() ?: 8,
            )
        }
    }
}
```

### 7. Inconsistent Dependency Declaration

**File:** `build.gradle.kts`  
**Line:** 27

**Issue:**
```kotlin
// Most dependencies use version catalog:
implementation(libs.kotlin.stdlib)
implementation(libs.hotlibs.database)

// But one is hardcoded:
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.10.2")
```

**Recommended Fix:**
Add to `libs.versions.toml` (or version catalog):
```toml
[versions]
kotlinx-coroutines = "1.10.2"

[libraries]
kotlinx-coroutines-jdk8 = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8", version.ref = "kotlinx-coroutines" }
```

Then use:
```kotlin
implementation(libs.kotlinx.coroutines.jdk8)
```

## Documentation Issues

### 8. Missing API Documentation

**Issue:** No OpenAPI/Swagger documentation for REST endpoints.

**Recommended Implementation:**

```kotlin
// Add to build.gradle.kts:
dependencies {
    implementation("io.ktor:ktor-server-openapi:$ktor_version")
    implementation("io.ktor:ktor-server-swagger:$ktor_version")
}

// In RouteConfig.kt or AppConfig.kt:
fun Application.configureOpenAPI() {
    routing {
        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
    }
}
```

### 9. Architecture Documentation

**Missing:**
- Architecture Decision Records (ADRs)
- Component diagram
- Data flow diagrams
- Integration architecture

**Recommended Structure:**
```
docs/
├── architecture/
│   ├── ADR-001-use-ktor-framework.md
│   ├── ADR-002-transaction-management.md
│   ├── component-diagram.md
│   └── data-flow.md
├── development/
│   ├── setup.md
│   ├── testing.md
│   └── troubleshooting.md
└── api/
    └── openapi.yaml
```

## Testing Issues

### 10. No Coverage Metrics

**Issue:** Cannot measure test coverage.

**Recommended Fix:**

```kotlin
// Add to build.gradle.kts:
plugins {
    jacoco
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
```

## Code Style Issues

### 11. Inconsistent Logging

**Issue:** Mix of logging styles and levels.

**Examples:**
```kotlin
// Some use structured logging:
log.info { "Delbestilling '$id' sendt inn med saksnummer '${delbestillingSak.saksnummer}'" }

// Some don't:
log.info { "Oppretter delbestilling for hmsnr $hmsnr, serienr $serienr" }

// Some log errors without context:
log.error(e) { "Klarte ikke å hente bruker fra PDL" }
```

**Recommended Guidelines:**

1. **Always use structured logging:**
```kotlin
log.info { 
    "Order created" to mapOf(
        "orderId" to id,
        "hmsnr" to hmsnr,
        "serienr" to serienr
    )
}
```

2. **Include correlation IDs:**
```kotlin
log.info { 
    "Order created | correlationId=$correlationId | orderId=$id" 
}
```

3. **Never log sensitive data:**
```kotlin
// BAD:
log.info { "User: $fnr" }

// GOOD:
log.info { "User: ${fnr.masked()}" }
```

## Performance Issues

### 12. Potential N+1 Queries

**File:** `DelbestillingService.kt`  
**Line:** 120-125

**Issue:**
```kotlin
val delerHmsnr = request.delbestilling.deler.map { it.del.hmsnr }
val lagerstatuser = oebs.hentLagerstatusForKommunenummer(brukerKommunenr, delerHmsnr)
val berikedeDellinjer = request.delbestilling.deler.map { dellinje ->
    val lagerstatus = checkNotNull(
        lagerstatuser.find { it.artikkelnummer == dellinje.del.hmsnr }
    ) { "Mangler lagerstatus for ${dellinje.del.hmsnr}" }
    dellinje.copy(lagerstatusPåBestillingstidspunkt = lagerstatus)
}
```

**Current:** Using `find` in a loop - O(n²) complexity

**Recommended Fix:**
```kotlin
val delerHmsnr = request.delbestilling.deler.map { it.del.hmsnr }
val lagerstatusMap = oebs.hentLagerstatusForKommunenummerAsMap(
    brukerKommunenr, 
    delerHmsnr
)
val berikedeDellinjer = request.delbestilling.deler.map { dellinje ->
    val lagerstatus = checkNotNull(
        lagerstatusMap[dellinje.del.hmsnr]
    ) { "Mangler lagerstatus for ${dellinje.del.hmsnr}" }
    dellinje.copy(lagerstatusPåBestillingstidspunkt = lagerstatus)
}
```

This uses the already-existing `hentLagerstatusForKommunenummerAsMap` method for O(n) complexity.

## Summary of Quick Wins

These can be fixed quickly with high impact:

1. ✅ **Remove duplicate exception handler** (5 minutes)
2. ✅ **Fix N+1 query** by using AsMap variant (10 minutes)
3. ✅ **Add Norg caching** (30 minutes)
4. ✅ **Move hardcoded version to catalog** (5 minutes)
5. ✅ **Create GitHub issues for all TODOs** (30 minutes)
6. ✅ **Add JaCoCo coverage** (15 minutes)
7. ✅ **Document business rules** (1 hour)

Total time for quick wins: ~3 hours
Impact: Significant improvement in code quality

---

*These issues should be prioritized and addressed in upcoming sprints.*
