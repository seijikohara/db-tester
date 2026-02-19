# DB Tester

A database testing framework for JUnit, Spock, and Kotest with multi-format test data management (CSV, TSV, JSON, YAML).

## Project Overview

See @README.md for project details.

## Modules

- `db-tester-api` - Public API (annotations, configuration, SPI)
- `db-tester-core` - Internal implementation (SPI providers, JDBC operations)
- `db-tester-spring-support` - Common Spring utilities for DataSource registration
- `db-tester-junit` - JUnit extension
- `db-tester-spock` - Spock extension
- `db-tester-kotest` - Kotest AnnotationSpec extension
- `db-tester-junit-spring-boot-starter` - Spring Boot starter for JUnit
- `db-tester-spock-spring-boot-starter` - Spring Boot starter for Spock
- `db-tester-kotest-spring-boot-starter` - Spring Boot starter for Kotest
- `db-tester-bom` - Bill of Materials

## Technology Stack

- Java 21 (via Gradle toolchain)
- Groovy 5 (for Spock module)
- Kotlin 2 (for Kotest module)
- Gradle wrapper with Kotlin DSL
- JUnit 6, Spock 2, Kotest 6
- Spring Boot 4 (for Spring Boot starters)

## Build Commands

| Command | Description |
|---------|-------------|
| `./gradlew build` | Full build (compile, test, format check) |
| `./gradlew :db-tester-core:build` | Single module build (avoids Testcontainers) |
| `./gradlew spotlessApply` | Format code (required before commit) |
| `./gradlew test` | Run all tests |
| `./gradlew verifyNullMarkedPackages` | Verify `@NullMarked` annotations |
| `npm run docs:dev` | Start VitePress dev server |
| `npm run docs:build` | Build VitePress documentation site |

### Build Gotchas

- `./gradlew build` includes `examples/` modules that use Testcontainers (requires Docker). Use module-specific builds (e.g., `:db-tester-core:build`) when Docker is unavailable.
- JaCoCo minimum coverage: 0.70. Use `-x jacocoTestCoverageVerification` for targeted test runs.
- `spotlessApply` required before commit. `build` also runs Checkstyle and Error Prone.
- Error Prone enforces: `final` on all parameters/locals, `LocalDateTime.now(ZoneId.systemDefault())`, `Duration.toSeconds()` not `getSeconds()`, `String.split(regex, limit)` not `split(regex)`.

### CI

- Tests run on Java 21 and Java 25. Required checks: `test (21)` and `test (25)`.

## Documentation

- Technical Specifications: @docs/specs/overview.md
- Architecture: @docs/specs/architecture.md
- API Reference: @docs/specs/public-api.md (landing page for annotations, dataset-interfaces, assertion-api, exceptions)
- Test Frameworks: @docs/specs/test-frameworks.md (landing page for junit, spock, kotest, spring-boot, lifecycle)
- SPI: @docs/specs/spi.md (landing page for spi-providers, spi-registration)
- Code Style Guides: [.claude/rules/](.claude/rules/)

## Git Workflow

- Commit format: Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`)
- Pre-commit: Run `./gradlew spotlessApply`, then `./gradlew build`
