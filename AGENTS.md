# AI Agent Instructions: DB Tester

> Coding standards and project conventions for AI coding assistants.

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Development](#development)
4. [Code Style](#code-style)

---

## Overview

### Purpose

A database testing framework for JUnit and Spock Framework with CSV-based test data management, built on DbUnit. The framework provides annotation-driven data preparation and validation.

### Key Features

| Feature | Description |
|---------|-------------|
| Convention over Configuration | Automatic file resolution based on test class/method names |
| Declarative Testing | `@Preparation` and `@Expectation` annotations for data setup and validation |
| Scenario-Based Testing | Share data files across multiple tests using scenario filtering (`[Scenario]` column) |
| Partial Column Validation | Validate only specific columns, ignore auto-generated fields |
| Multi-Framework Support | JUnit and Spock Framework integration |
| Multi-Database Support | Register and test multiple data sources simultaneously |
| Programmatic Assertions | Advanced validation via `DatabaseAssertion` API |
| Spring Boot Integration | Auto-configuration with automatic DataSource discovery |

### Modules

| Module | Description | Documentation |
|--------|-------------|---------------|
| `db-tester-core` | Core library with domain, application, and infrastructure layers | [README](db-tester-core/README.md) |
| `db-tester-junit` | JUnit Extension | [README](db-tester-junit/README.md) |
| `db-tester-spock` | Spock Framework Extension | [README](db-tester-spock/README.md) |
| `db-tester-junit-spring-boot-starter` | Spring Boot Starter for JUnit | [README](db-tester-junit-spring-boot-starter/README.md) |
| `db-tester-spock-spring-boot-starter` | Spring Boot Starter for Spock | [README](db-tester-spock-spring-boot-starter/README.md) |
| `db-tester-bom` | Bill of Materials | [README](db-tester-bom/README.md) |
| `examples` | Usage examples | [README](examples/README.md) |

### Technology Stack

| Category | Version |
|----------|---------|
| Java | 21 (via Gradle toolchain) |
| Groovy | 5 (for Spock module) |
| Build Tool | Gradle wrapper with Kotlin DSL |
| Testing | JUnit 6, Spock Framework 2, DbUnit 3, Testcontainers 2 |
| Databases | Any JDBC-compatible (tested: H2, MySQL, PostgreSQL, Derby, HSQLDB, MS SQL Server, Oracle) |
| Spring Boot | 4 (for Spring Boot Starter) |

---

## Architecture

### Simplified Two-Layer Architecture

The framework follows a simplified architecture with clear separation between public API and internal implementation:

| Layer | Package | Responsibility |
|-------|---------|----------------|
| Public API | `api/` | User-facing annotations, configuration, exceptions |
| Internal | `internal/` | Implementation details with DDD patterns |

### Module Dependencies

```
db-tester-bom
    │
    ├── db-tester-junit-spring-boot-starter
    │       └── db-tester-junit
    │               └── db-tester-core
    │
    ├── db-tester-spock-spring-boot-starter
    │       └── db-tester-spock
    │               └── db-tester-core
    │
    └── db-tester-spock
            └── db-tester-core
```

### Package Structure

```
io.github.seijikohara.dbtester
│
├── api/                                    # Public API (Stable Interface)
│   ├── annotation/                         # User-facing annotations
│   │   ├── Preparation.java                # Test data setup annotation
│   │   ├── Expectation.java                # Test verification annotation
│   │   └── DataSet.java                    # Dataset configuration
│   ├── assertion/                          # Programmatic assertion API
│   │   ├── DatabaseAssertion.java          # Facade for assertions
│   │   └── AssertionFailureHandler.java    # Callback interface
│   ├── config/                             # Configuration API
│   │   ├── Configuration.java              # Main configuration
│   │   ├── DataSourceRegistry.java         # DataSource management
│   │   ├── ConventionSettings.java         # Convention configuration
│   │   └── OperationDefaults.java          # Default operation settings
│   ├── exception/                          # User-facing exceptions
│   │   ├── DatabaseTesterException.java    # Base exception
│   │   ├── DataSetLoadException.java       # Dataset loading errors
│   │   ├── ConfigurationException.java     # Configuration errors
│   │   └── ValidationException.java        # Validation errors
│   ├── loader/                             # DataSet loader interface
│   │   └── DataSetLoader.java              # SPI for custom loaders
│   └── operation/                          # Database operations
│       └── Operation.java                  # Operation enum (CLEAN_INSERT, etc.)
│
└── internal/                               # Internal Implementation (DDD structure)
    │
    ├── domain/                             # Value Objects (Type-safe identifiers)
    │   ├── TableName.java                  # Table name wrapper
    │   ├── ColumnName.java                 # Column name wrapper
    │   ├── ScenarioName.java               # Scenario name wrapper
    │   ├── ScenarioMarker.java             # Scenario marker column
    │   ├── DataSourceName.java             # DataSource name wrapper
    │   ├── SchemaName.java                 # Schema name wrapper
    │   ├── DataValue.java                  # Cell value wrapper
    │   ├── FileExtension.java              # File extension wrapper
    │   └── StringIdentifier.java           # Base interface for identifiers
    │
    ├── dataset/                            # DataSet domain model
    │   ├── DataSet.java                    # DataSet interface
    │   ├── Table.java                      # Table interface
    │   ├── Row.java                        # Row interface
    │   ├── SimpleDataSet.java              # Simple implementation
    │   ├── SimpleTable.java                # Simple implementation
    │   ├── SimpleRow.java                  # Simple implementation
    │   ├── ScenarioDataSet.java            # Scenario-filtered DataSet
    │   ├── ScenarioTable.java              # Scenario-filtered Table
    │   ├── ScenarioDataSetFactory.java     # Factory for scenario datasets
    │   ├── TableOrderingResolver.java      # FK-aware table ordering
    │   ├── DataSetFormatProvider.java      # SPI for format providers
    │   ├── DataSetFormatRegistry.java      # Format provider registry
    │   └── scenario/csv/                   # CSV scenario support
    │       ├── CsvFormatProvider.java      # CSV format provider
    │       ├── CsvScenarioDataSet.java     # CSV-based scenario dataset
    │       ├── CsvScenarioTable.java       # CSV-based scenario table
    │       └── CsvRow.java                 # CSV row implementation
    │
    ├── assertion/                          # Assertion implementations
    │   └── DataSetComparator.java          # DataSet comparison logic
    │
    ├── context/                            # Test execution context
    │   └── TestContext.java                # Immutable test context
    │
    ├── dbunit/                             # DbUnit bridge layer (complete isolation)
    │   ├── DatabaseBridge.java             # Primary interface (Singleton)
    │   ├── DbUnitOperations.java           # DbUnit operation execution
    │   ├── DbUnitDataSetAdapter.java       # internal.DataSet → IDataSet
    │   ├── TypeConverter.java              # Type conversion utilities
    │   ├── OperationConverter.java         # Operation enum to DbUnit
    │   ├── adapter/                        # DbUnit adapters
    │   │   ├── DbUnitDataSetAdapter.java   # IDataSet → internal.DataSet
    │   │   ├── DbUnitTableAdapter.java     # ITable → internal.Table
    │   │   └── DbUnitRowAdapter.java       # Row adapter
    │   ├── assertion/                      # DbUnit assertion utilities
    │   │   ├── DatabaseAssert.java         # Assertion facade
    │   │   ├── DataSetComparator.java      # IDataSet comparison
    │   │   ├── TableComparator.java        # ITable comparison
    │   │   ├── ColumnFilter.java           # Column filtering
    │   │   └── FailureHandlerAdapter.java  # Failure handler
    │   └── format/                         # Format readers
    │       ├── DataSetReader.java          # Reader interface
    │       └── CsvDataSetReader.java       # CSV reader
    │
    ├── loader/                             # Data loading implementations
    │   ├── TestClassNameBasedDataSetLoader.java  # Convention-based loader
    │   ├── DataSetLoaderProviderImpl.java  # SPI implementation
    │   ├── AnnotationResolver.java         # Annotation metadata extraction
    │   ├── DataSetFactory.java             # Dataset creation
    │   └── DirectoryResolver.java          # Directory resolution
    │
    ├── bridge/                             # Database bridge implementation
    │   └── DatabaseBridgeProviderImpl.java # SPI implementation
    │
    └── spi/                                # Service provider interfaces
        ├── DataSetLoaderProvider.java      # DataSet loader SPI
        ├── DatabaseBridgeProvider.java     # Database bridge SPI
        ├── ScenarioNameResolver.java       # Scenario name resolution SPI
        └── ScenarioNameResolverRegistry.java # Resolver registry
```

### DDD Patterns in Internal Package

| Pattern | Implementation |
|---------|----------------|
| Value Object | `TableName`, `ColumnName`, `DataValue`, `ScenarioName` - immutable, equality by value |
| Entity | `Table`, `Row` - have identity within aggregate |
| Aggregate | `DataSet` (root) - consistency boundary |
| Domain Service | `DataSetComparator` - comparison logic |
| Factory | `ScenarioDataSetFactory`, `DataSetFactory` - complex object creation |
| Adapter | `DbUnitDataSetAdapter`, `DbUnitTableAdapter` - bridge to external library |

### SPI Interfaces

| Port | Purpose | Implementations |
|------|---------|-----------------|
| `ScenarioNameResolver` | Resolve scenario names from test methods | `JUnitScenarioNameResolver`, `SpockScenarioNameResolver` |
| `DataSetLoaderProvider` | Load datasets from resources | `DataSetLoaderProviderImpl` |
| `DatabaseBridgeProvider` | Database operations | `DatabaseBridgeProviderImpl` |
| `DataSetFormatProvider` | Parse dataset files | `CsvFormatProvider` |

**SPI Registration** (in `META-INF/services/`):

| Service | File | Implementations |
|---------|------|-----------------|
| Scenario | `io.github.seijikohara.dbtester.internal.spi.ScenarioNameResolver` | JUnit, Spock resolvers |
| Format | `io.github.seijikohara.dbtester.internal.dataset.DataSetFormatProvider` | CSV format provider |
| Loader | `io.github.seijikohara.dbtester.internal.spi.DataSetLoaderProvider` | Convention-based loader |
| Bridge | `io.github.seijikohara.dbtester.internal.spi.DatabaseBridgeProvider` | DbUnit bridge |

### Java Platform Module System (JPMS)

All modules provide `Automatic-Module-Name` in the JAR manifest for JPMS compatibility.

| Module | Automatic-Module-Name |
|--------|----------------------|
| db-tester-core | `io.github.seijikohara.dbtester.core` |
| db-tester-junit | `io.github.seijikohara.dbtester.junit` |
| db-tester-spock | `io.github.seijikohara.dbtester.spock` |
| db-tester-junit-spring-boot-starter | `io.github.seijikohara.dbtester.junit.spring.autoconfigure` |
| db-tester-spock-spring-boot-starter | `io.github.seijikohara.dbtester.spock.spring.autoconfigure` |

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
./gradlew :db-tester-junit-spring-boot-starter:build
./gradlew :db-tester-spock-spring-boot-starter:build

# Examples only
./gradlew :examples:db-tester-example-junit:test
./gradlew :examples:db-tester-example-spock:test
./gradlew :examples:db-tester-example-junit-spring-boot-starter:test
./gradlew :examples:db-tester-example-spock-spring-boot-starter:test

# Javadoc generation
./gradlew javadoc

# Verify @NullMarked annotations in all packages
./gradlew verifyNullMarkedPackages
```

### Testing

**Test locations**:

| Directory | Purpose | Database |
|-----------|---------|----------|
| `db-tester-core/src/test/java` | Core library unit tests | Mock only |
| `db-tester-junit/src/test/java` | JUnit extension unit tests | Mock only |
| `db-tester-spock/src/test/groovy` | Spock extension unit tests | Mock only |
| `db-tester-junit-spring-boot-starter/src/test/java` | JUnit Spring Boot Starter unit tests | Mock only |
| `db-tester-spock-spring-boot-starter/src/test/groovy` | Spock Spring Boot Starter unit tests | Mock only |
| `examples/db-tester-example-junit/src/test/java` | JUnit usage examples | H2 in-memory |
| `examples/db-tester-example-spock/src/test/groovy` | Spock usage examples | H2 in-memory |
| `examples/db-tester-example-junit-spring-boot-starter/src/test/java` | JUnit Spring Boot usage examples | H2 in-memory |
| `examples/db-tester-example-spock-spring-boot-starter/src/test/groovy` | Spock Spring Boot usage examples | H2 in-memory |

**Unit test requirements** (library modules):
- Fast execution (less than 1 second per test)
- No database connections (use Mockito for mocking)
- Test one class in isolation

### Static Analysis

All static analysis runs automatically during compilation. Violations cause build failures.

| Tool | Enforces | Fix |
|------|----------|-----|
| NullAway | Null safety at compile time | Add `@Nullable` for nullable parameters |
| DocLint | Complete Javadoc for public API | Add missing `@param`, `@return`, `@throws` |
| Spotless | Google Java Format | Run `./gradlew spotlessApply` |
| verifyNullMarkedPackages | `@NullMarked` in all `package-info.java` | Create missing `package-info.java` with `@NullMarked` |

### Git Workflow

**Commit format** (Conventional Commits):

```
<type>: <description>

[optional body]

[optional footer]
```

**Types**: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `build`, `ci`, `perf`, `style`

**Examples**:
```
feat: add PostgreSQL JSONB support
fix: resolve NPE in DataSet loader
refactor: simplify table ordering logic
```

**Pre-commit checklist**:

1. Format code: `./gradlew spotlessApply`
2. Build succeeds: `./gradlew build`
3. Commit message uses Conventional Commits format
4. Each commit focuses on one logical change

**CI requirements**:

```bash
./gradlew build    # Compilation + NullAway + DocLint + Tests + Spotless
./gradlew javadoc  # Javadoc generation
```

---

## Code Style

Detailed coding standards are documented in separate files:

| Document | Scope |
|----------|-------|
| [docs/code-styles/JAVA.md](docs/code-styles/JAVA.md) | Java production code |
| [docs/code-styles/JAVA_TESTING.md](docs/code-styles/JAVA_TESTING.md) | Java test code (JUnit) |
| [docs/code-styles/GROOVY.md](docs/code-styles/GROOVY.md) | Groovy production code |
| [docs/code-styles/GROOVY_TESTING.md](docs/code-styles/GROOVY_TESTING.md) | Groovy test code (Spock Framework) |
| [docs/code-styles/DOCUMENTATION.md](docs/code-styles/DOCUMENTATION.md) | Documentation (Javadoc, Markdown, comments, commits) |

### Java Production Code Summary

Key rules from [JAVA.md](docs/code-styles/JAVA.md):

- **Variables**: All parameters and local variables must be `final`; use `final var`
- **Null Safety**: Every package requires `@NullMarked` in `package-info.java`
- **Immutability**: All classes must be immutable; no setters; return immutable collections
- **Functional**: Use Stream API; avoid for/while loops
- **Optional**: Return `Optional<T>` for absent values; use `@Nullable` for parameters
- **Exceptions**: Catch specific exception types; wrap external exceptions at module boundaries
- **Javadoc**: Required on all public classes and methods; enforced by DocLint

### Java Test Code Summary

Key rules from [JAVA_TESTING.md](docs/code-styles/JAVA_TESTING.md):

- **Class structure**: `@DisplayName` annotation on class; `@Nested` classes for each method under test
- **Method naming**: `should[Expected]_when[Condition]` pattern
- **Annotations**: `@Test`, `@Tag("normal"/"edge-case"/"error")`, `@DisplayName`
- **AAA pattern**: Use `// Given`, `// When`, `// Then` comments to structure test code
- **Assertions**: Use `assertAll` with descriptive messages for all assertions
- **Javadoc**: Required on all test classes, constructors, and methods
- **Null Safety**: All packages (including tests and examples) require `@NullMarked` in `package-info.java`

### Groovy Production Code Summary

Key rules from [GROOVY.md](docs/code-styles/GROOVY.md):

- **Syntax**: Omit semicolons; omit parentheses for top-level calls
- **Type declarations**: Use explicit types for public APIs; `def` for local variables
- **Visibility**: Omit `public` modifier (Groovy default)
- **Property access**: Use `object.property` instead of `object.getProperty()`
- **Null safety**: Use safe navigation `?.` and Elvis operator `?:`
- **Strings**: Use GStrings with `${}` for interpolation; triple quotes for multiline
- **Collections**: Use native syntax `[]` for lists, `[:]` for maps
- **Javadoc**: Required on all classes and methods; enforced by DocLint

### Groovy Test Code Summary (Spock Framework)

Key rules from [GROOVY_TESTING.md](docs/code-styles/GROOVY_TESTING.md):

- **Class structure**: Extend `Specification`; use `Spec` suffix
- **Feature naming**: Use descriptive string literals (e.g., `'should load data'`)
- **Block structure**: Use `given:`/`when:`/`then:` or `expect:` with descriptions
- **Assertions**: Conditions in `then:`/`expect:` are implicit assertions; use `verifyAll` for groups
- **Fields**: Use `@Shared` for expensive resources; static for framework integration
- **Data-driven**: Use data tables with `where:` block; `||` separates inputs from outputs
- **Testcontainers**: Use `@Testcontainers` with `@Shared` containers

### Naming Conventions

| Layer | Suffix | Example |
|-------|--------|---------|
| Public API | none | `Configuration`, `DataSourceRegistry`, `Operation` |
| Internal Model | none | `DataSet`, `Table`, `Row` |
| Internal Value Object | none | `TableName`, `ColumnName`, `DataValue` |
| Internal Service | Comparator/Factory | `DataSetComparator`, `ScenarioDataSetFactory` |
| Internal SPI | Port/Provider/Resolver | `DataSetLoaderProvider`, `ScenarioNameResolver` |
| Internal Adapter | Adapter | `DbUnitDataSetAdapter`, `DbUnitTableAdapter` |
| Test Framework Resolver | Resolver | `JUnitScenarioNameResolver`, `SpockScenarioNameResolver` |
