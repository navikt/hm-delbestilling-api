# Code Quality Review - Action Plan

## Overview
This document provides a prioritized action plan based on the code quality review of `hm-delbestilling-api`.

## Immediate Actions (This Week)

### 1. Fix Critical Bug - Duplicate Exception Handler
**File:** `src/main/kotlin/no/nav/hjelpemidler/delbestilling/config/ErrorHandling.kt`  
**Effort:** 5 minutes  
**Impact:** High - Fixes potential runtime bug

Remove the duplicate `PdlResponseMissingData` exception handler (lines 35-37).

### 2. Fix Performance Issue - N+1 Query
**File:** `src/main/kotlin/no/nav/hjelpemidler/delbestilling/delbestilling/DelbestillingService.kt`  
**Lines:** 120-125  
**Effort:** 10 minutes  
**Impact:** High - Improves performance

Replace `lagerstatuser.find()` loop with `hentLagerstatusForKommunenummerAsMap()`.

### 3. Create GitHub Issues for TODOs
**Effort:** 30 minutes  
**Impact:** Medium - Improves tracking

Create 8 GitHub issues for existing TODOs to track technical debt.

## Short-term Actions (Next Sprint)

### 4. Implement Caching for NORG
**File:** `src/main/kotlin/no/nav/hjelpemidler/delbestilling/infrastructure/norg/Norg.kt`  
**Effort:** 30 minutes  
**Impact:** High - Reduces external API calls

Use Caffeine cache (already a dependency) to cache NORG lookups.

### 5. Add Code Coverage Metrics
**File:** `build.gradle.kts`  
**Effort:** 1 hour  
**Impact:** High - Enables tracking test coverage

Add JaCoCo plugin and configure coverage reports.

### 6. Extract Configuration from Business Logic
**Files:** Multiple service files  
**Effort:** 2-3 hours  
**Impact:** High - Improves testability

Create `ValidationConfig` class to replace environment checks (`isDev()`, `isProd()`).

### 7. Standardize Dependency Management
**File:** `build.gradle.kts`  
**Effort:** 15 minutes  
**Impact:** Low - Improves consistency

Move `kotlinx-coroutines-jdk8` to version catalog.

## Medium-term Actions (Next 2-3 Sprints)

### 8. Refactor Large Data Files
**Files:** `Deler.kt`, `Hjelpemidler.kt`, `DelerPerHjelpemiddel.kt`  
**Effort:** 1 week  
**Impact:** High - Major maintainability improvement

Extract hardcoded data to JSON files with repository pattern.

**Steps:**
1. Create JSON schemas for data
2. Export existing data to JSON files
3. Create repository classes
4. Update tests
5. Deploy and verify

### 9. Refactor DelbestillingService
**File:** `DelbestillingService.kt` (257 lines)  
**Effort:** 1 week  
**Impact:** High - Improves maintainability

Split into multiple focused classes:
- `DelbestillingValidator` - All validation logic
- `DelbestillingPreparator` - Order preparation
- `DelbestillingService` - Orchestration only

### 10. Add API Documentation
**Effort:** 2-3 days  
**Impact:** Medium - Improves developer experience

- Add OpenAPI/Swagger documentation
- Document all REST endpoints
- Add request/response examples
- Include authentication requirements

### 11. Improve Logging Standards
**Effort:** 1-2 days  
**Impact:** Medium - Improves observability

- Create logging guidelines document
- Implement structured logging consistently
- Add correlation IDs to all logs
- Ensure no sensitive data in logs

## Long-term Actions (Next Quarter)

### 12. Architecture Documentation
**Effort:** 1 week  
**Impact:** Medium - Improves onboarding

Create comprehensive documentation:
- Architecture Decision Records (ADRs)
- Component diagrams
- Data flow diagrams
- Integration architecture
- Domain model documentation

### 13. Code Style Automation
**Effort:** 2-3 days  
**Impact:** Medium - Ensures consistency

- Add ktlint or detekt
- Configure rules
- Add to CI/CD pipeline
- Fix existing violations
- Add pre-commit hooks

### 14. Enhanced Testing
**Effort:** Ongoing  
**Impact:** High - Reduces bugs

- Increase coverage to >80%
- Add integration tests for complete flows
- Add contract tests for external APIs
- Implement mutation testing

### 15. Security Enhancements
**Effort:** 1-2 weeks  
**Impact:** High - Reduces security risk

- Add dependency scanning (Snyk/Dependabot)
- Implement audit logging for sensitive operations
- Review all personal data handling
- Add security scanning to CI/CD
- Regular security audits

## Estimated Timeline

```
Week 1-2:
├── Fix critical bug (duplicate exception handler)
├── Fix N+1 query performance issue
├── Create GitHub issues for TODOs
├── Implement NORG caching
└── Add code coverage metrics

Week 3-4:
├── Extract configuration from business logic
├── Standardize dependency management
└── Begin planning large data file refactor

Month 2:
├── Refactor large data files
├── Refactor DelbestillingService
└── Add API documentation

Month 3:
├── Improve logging standards
├── Architecture documentation
└── Code style automation

Ongoing:
├── Enhanced testing
├── Security enhancements
└── Technical debt reduction
```

## Success Metrics

Track these metrics to measure improvement:

| Metric | Current | Target (3 months) |
|--------|---------|-------------------|
| TODO Count | 8 | 0 |
| Code Coverage | Unknown | >80% |
| Largest File | 1,397 LOC | <500 LOC |
| Environment Checks | ~5 in main logic | 0 |
| Critical Bugs | 1 (duplicate handler) | 0 |
| Documentation Pages | 1 (README) | 10+ |
| Build Time | Unknown | <3 min |
| Sonar Quality Gate | Unknown | Pass |

## Resource Requirements

- **Development Time:** ~6-8 weeks of engineering time spread over 3 months
- **Review Time:** ~1-2 hours per week for code reviews
- **Documentation Time:** ~1 week total
- **Team Size:** Can be distributed across 2-3 developers

## Risk Mitigation

1. **Large Refactors:** Do incrementally with feature flags
2. **Data Extraction:** Keep old code in place until verified
3. **Breaking Changes:** Version APIs and maintain backwards compatibility
4. **Production Issues:** Thorough testing in dev environment first

## Dependencies

- No new external dependencies required for most tasks
- JaCoCo for code coverage (testing tool)
- OpenAPI plugin for API docs (optional)
- ktlint/detekt for style checking (optional)

## Communication Plan

1. **Weekly Updates:** Progress on action items in team standup
2. **Bi-weekly Reviews:** Code review sessions for refactored code
3. **Monthly Reports:** Metrics and progress updates to stakeholders
4. **Documentation:** Keep this document updated with progress

## Getting Started

To begin implementing this action plan:

1. **Review with team** - Discuss priorities and timeline
2. **Create GitHub project** - Track all action items
3. **Assign owners** - Each task needs an owner
4. **Start with quick wins** - Build momentum
5. **Regular check-ins** - Weekly progress reviews

## Questions?

For questions about this action plan, contact the development team or create a discussion in GitHub.

---

**Last Updated:** 2025-12-10  
**Next Review:** 2026-01-10
