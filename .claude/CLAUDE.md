# DB Tester

A database testing framework for JUnit, Spock, and Kotest with CSV-based test data management.

## Project Overview

See @README.md for detailed project information.

## Modules

- `db-tester-api` - Public API (annotations, configuration, SPI interfaces)
- `db-tester-core` - Internal implementation (SPI providers, JDBC operations)
- `db-tester-junit` - JUnit Extension
- `db-tester-spock` - Spock Extension
- `db-tester-kotest` - Kotest AnnotationSpec Extension
- `db-tester-junit-spring-boot-starter` - Spring Boot Starter for JUnit
- `db-tester-spock-spring-boot-starter` - Spring Boot Starter for Spock
- `db-tester-kotest-spring-boot-starter` - Spring Boot Starter for Kotest
- `db-tester-bom` - Bill of Materials

## Technology Stack

- Java 21 (via Gradle toolchain)
- Groovy 5 (for Spock module)
- Kotlin 2 (for Kotest module)
- Gradle wrapper with Kotlin DSL
- JUnit 6, Spock 2, Kotest 6
- Spring Boot 4 (for Spring Boot Starter)

## Build Commands

- `./gradlew build` - Full build (compile + test + format check)
- `./gradlew spotlessApply` - Format code (required before commit)
- `./gradlew test` - Run all tests
- `./gradlew verifyNullMarkedPackages` - Verify @NullMarked annotations

## Documentation

- Technical Specifications: @docs/specs/01-overview.md
- Architecture: @docs/specs/02-architecture.md
- Public API: @docs/specs/03-public-api.md
- Code Style Guides: [.claude/rules/](.claude/rules/) (auto-loaded by Claude Code)

## Git Workflow

- Commit format: Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`)
- Pre-commit: Run `./gradlew spotlessApply` then `./gradlew build`
