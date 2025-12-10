# AI Agent Instructions: DB Tester

> Coding standards and project conventions for AI coding assistants.

---

## Table of Contents

1. [Overview](#overview)
2. [Documentation](#documentation)
3. [Development](#development)
4. [Code Style](#code-style)

---

## Overview

A database testing framework for JUnit and Spock Framework with CSV-based test data management. The framework provides annotation-driven data preparation and validation using pure JDBC operations.

### Key Features

| Feature | Description |
|---------|-------------|
| Convention over Configuration | File resolution based on test class/method names |
| Declarative Testing | `@Preparation` and `@Expectation` annotations for data setup and validation |
| Scenario-Based Testing | Share data files across multiple tests using scenario filtering |
| Multi-Framework Support | JUnit and Spock Framework integration |
| Multi-Database Support | Register and test multiple data sources simultaneously |
| Spring Boot Integration | Auto-configuration with DataSource discovery |

### Modules

| Module | Description |
|--------|-------------|
| `db-tester-api` | Public API (annotations, configuration, SPI interfaces) |
| `db-tester-core` | Internal implementation (SPI providers, JDBC operations) |
| `db-tester-junit` | JUnit Extension |
| `db-tester-spock` | Spock Framework Extension |
| `db-tester-junit-spring-boot-starter` | Spring Boot Starter for JUnit |
| `db-tester-spock-spring-boot-starter` | Spring Boot Starter for Spock |
| `db-tester-bom` | Bill of Materials |

### Technology Stack

| Category | Version |
|----------|---------|
| Java | 21 (via Gradle toolchain) |
| Groovy | 5 (for Spock module) |
| Build Tool | Gradle wrapper with Kotlin DSL |
| Testing | JUnit 6, Spock Framework 2 |
| Spring Boot | 4 (for Spring Boot Starter) |

---

## Documentation

### Technical Specifications

Detailed specifications are available in `docs/specs/`:

| Document | Description |
|----------|-------------|
| [Overview](docs/specs/01-OVERVIEW.md) | Framework purpose and key concepts |
| [Architecture](docs/specs/02-ARCHITECTURE.md) | Module structure and dependencies |
| [Public API](docs/specs/03-PUBLIC-API.md) | Annotations and configuration classes |
| [Configuration](docs/specs/04-CONFIGURATION.md) | Configuration options and conventions |
| [Data Formats](docs/specs/05-DATA-FORMATS.md) | CSV/TSV file structure and parsing |
| [Database Operations](docs/specs/06-DATABASE-OPERATIONS.md) | Supported operations and execution flow |
| [Test Frameworks](docs/specs/07-TEST-FRAMEWORKS.md) | JUnit and Spock integration |
| [SPI](docs/specs/08-SPI.md) | Service Provider Interface extension points |
| [Error Handling](docs/specs/09-ERROR-HANDLING.md) | Error messages and exception types |

### Code Style Guides

| Document | Scope |
|----------|-------|
| [JAVA.md](docs/code-styles/JAVA.md) | Java production code |
| [JAVA_TESTING.md](docs/code-styles/JAVA_TESTING.md) | Java test code (JUnit) |
| [GROOVY.md](docs/code-styles/GROOVY.md) | Groovy production code |
| [GROOVY_TESTING.md](docs/code-styles/GROOVY_TESTING.md) | Groovy test code (Spock Framework) |
| [DOCUMENTATION.md](docs/code-styles/DOCUMENTATION.md) | Javadoc, Markdown, comments, commits |

### Other Documentation

| Document | Description |
|----------|-------------|
| [PUBLISHING.md](docs/PUBLISHING.md) | Maven Central publishing process |
| [RELEASE.md](docs/RELEASE.md) | Release procedures |

---

## Development

### Build Commands

```bash
# Full build (compile + test + format check)
./gradlew build

# Format code (required before commit)
./gradlew spotlessApply

# Run all tests
./gradlew test

# Specific module builds
./gradlew :db-tester-core:build
./gradlew :db-tester-junit:build
./gradlew :db-tester-spock:build

# Examples only
./gradlew :examples:db-tester-example-junit:test
./gradlew :examples:db-tester-example-spock:test

# Javadoc generation
./gradlew javadoc

# Verify @NullMarked annotations
./gradlew verifyNullMarkedPackages
```

### Testing

| Directory | Purpose | Database |
|-----------|---------|----------|
| `db-tester-*/src/test/` | Library unit tests | Mock only |
| `examples/*/src/test/` | Usage examples | H2 in-memory |

**Unit test requirements** (library modules):
- Fast execution (less than 1 second per test)
- No database connections (use Mockito for mocking)
- Test one class in isolation

### Static Analysis

| Tool | Enforces | Fix |
|------|----------|-----|
| NullAway | Null safety at compile time | Add `@Nullable` for nullable parameters |
| DocLint | Complete Javadoc for public API | Add missing `@param`, `@return`, `@throws` |
| Spotless | Google Java Format | Run `./gradlew spotlessApply` |
| verifyNullMarkedPackages | `@NullMarked` in all packages | Create `package-info.java` with `@NullMarked` |

### Git Workflow

**Commit format** (Conventional Commits):

```
<type>: <description>

[optional body]

[optional footer]
```

**Types**: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `build`, `ci`, `perf`, `style`

**Pre-commit checklist**:

1. Format code: `./gradlew spotlessApply`
2. Build succeeds: `./gradlew build`
3. Commit message uses Conventional Commits format

---

## Code Style

### Quick Reference

| Category | Rule |
|----------|------|
| Variables | All parameters and local variables must be `final` |
| Null Safety | Every package requires `@NullMarked` in `package-info.java` |
| Immutability | All classes must be immutable; no setters |
| Functional | Use Stream API; avoid for/while loops |
| Javadoc | Required on all public and private classes/methods |

### Naming Conventions

| Layer | Suffix | Example |
|-------|--------|---------|
| Public API | none | `Configuration`, `DataSourceRegistry` |
| Internal Model | none | `DataSet`, `Table`, `Row` |
| Internal Service | Comparator/Verifier | `DataSetComparator`, `ExpectationVerifier` |
| Internal SPI | Provider/Resolver | `DataSetLoaderProvider`, `ScenarioNameResolver` |
| JDBC Operation | Executor | `InsertExecutor`, `UpdateExecutor` |

For detailed rules, see [Code Style Guides](#code-style-guides).
