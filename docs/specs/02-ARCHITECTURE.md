# DB Tester Specification - Architecture

This document describes the module structure, dependencies, and architectural patterns of the DB Tester framework.

---

## Table of Contents

1. [Module Structure](#module-structure)
2. [Module Dependencies](#module-dependencies)
3. [Package Organization](#package-organization)
4. [Architectural Patterns](#architectural-patterns)
5. [JPMS Support](#jpms-support)

---

## Module Structure

The framework consists of seven modules organized in a layered architecture:

```
db-tester-bom
├── db-tester-api
├── db-tester-core
│   └── db-tester-api
├── db-tester-junit
│   ├── db-tester-api (compile)
│   └── db-tester-core (runtime via ServiceLoader)
├── db-tester-spock
│   ├── db-tester-api (compile)
│   └── db-tester-core (runtime via ServiceLoader)
├── db-tester-junit-spring-boot-starter
│   └── db-tester-junit
└── db-tester-spock-spring-boot-starter
    └── db-tester-spock
```

### Module Responsibilities

| Module | Responsibility |
|--------|----------------|
| `db-tester-bom` | Version management and dependency alignment |
| `db-tester-api` | Public annotations, configuration, SPI interfaces |
| `db-tester-core` | JDBC operations, format parsing, SPI implementations |
| `db-tester-junit` | JUnit Jupiter BeforeEach/AfterEach callbacks |
| `db-tester-spock` | Spock global extension and interceptors |
| `db-tester-junit-spring-boot-starter` | Spring Boot auto-configuration for JUnit |
| `db-tester-spock-spring-boot-starter` | Spring Boot auto-configuration for Spock |

---

## Module Dependencies

### API Module (`db-tester-api`)

The API module has no internal dependencies. External dependencies:

| Dependency | Purpose |
|------------|---------|
| `org.jspecify:jspecify` | Null safety annotations |
| `java.sql` | `DataSource` interface |

### Core Module (`db-tester-core`)

| Dependency | Scope | Purpose |
|------------|-------|---------|
| `db-tester-api` | Compile | Public API classes |
| `org.jspecify:jspecify` | Compile | Null safety annotations |
| `org.slf4j:slf4j-api` | Compile | Logging abstraction |

### Test Framework Modules

JUnit and Spock modules depend on `db-tester-api` at compile time. The `db-tester-core` module is discovered at runtime via ServiceLoader.

| Module | Compile Dependencies | Runtime Dependencies |
|--------|---------------------|----------------------|
| `db-tester-junit` | `db-tester-api`, `junit-jupiter-api` | `db-tester-core` |
| `db-tester-spock` | `db-tester-api`, `spock-core` | `db-tester-core` |

### Spring Boot Starter Modules

| Module | Dependencies |
|--------|--------------|
| `db-tester-junit-spring-boot-starter` | `db-tester-junit`, `db-tester-core`, `spring-boot-autoconfigure` |
| `db-tester-spock-spring-boot-starter` | `db-tester-spock`, `db-tester-core`, `spring-boot-autoconfigure` |

---

## Package Organization

### API Module Packages

```
io.github.seijikohara.dbtester.api
├── annotation/
│   ├── Preparation.java
│   ├── Expectation.java
│   └── DataSet.java
├── assertion/
│   ├── DatabaseAssertion.java
│   └── AssertionFailureHandler.java
├── config/
│   ├── Configuration.java
│   ├── ConventionSettings.java
│   ├── DataFormat.java
│   ├── DataSourceRegistry.java
│   ├── OperationDefaults.java
│   └── TableMergeStrategy.java
├── context/
│   └── TestContext.java
├── dataset/
│   ├── DataSet.java
│   ├── Table.java
│   └── Row.java
├── domain/
│   ├── CellValue.java
│   ├── ColumnName.java
│   ├── TableName.java
│   ├── DataSourceName.java
│   ├── Column.java
│   ├── Cell.java
│   ├── ColumnMetadata.java
│   └── ComparisonStrategy.java
├── exception/
│   ├── DatabaseTesterException.java
│   ├── ConfigurationException.java
│   ├── DataSetLoadException.java
│   ├── DataSourceNotFoundException.java
│   ├── DatabaseOperationException.java
│   └── ValidationException.java
├── loader/
│   └── DataSetLoader.java
├── operation/
│   └── Operation.java
├── scenario/
│   ├── ScenarioName.java
│   └── ScenarioNameResolver.java
└── spi/
    ├── AssertionProvider.java
    ├── DataSetLoaderProvider.java
    ├── ExpectationProvider.java
    └── OperationProvider.java
```

### Core Module Packages

```
io.github.seijikohara.dbtester.internal
├── assertion/
│   ├── DataSetComparator.java
│   └── ExpectationVerifier.java
├── dataset/
│   ├── SimpleDataSet.java
│   ├── SimpleTable.java
│   ├── SimpleRow.java
│   ├── ScenarioTable.java
│   └── TableOrderingResolver.java
├── domain/
│   ├── ScenarioMarker.java
│   ├── SchemaName.java
│   ├── FileExtension.java
│   └── StringIdentifier.java
├── format/
│   ├── csv/CsvFormatProvider.java
│   ├── tsv/TsvFormatProvider.java
│   ├── parser/DelimitedParser.java
│   └── spi/FormatProvider.java
├── jdbc/
│   ├── OperationExecutor.java
│   ├── InsertExecutor.java
│   ├── UpdateExecutor.java
│   ├── DeleteExecutor.java
│   ├── RefreshExecutor.java
│   ├── TruncateExecutor.java
│   ├── SqlBuilder.java
│   ├── ParameterBinder.java
│   ├── ValueParser.java
│   ├── TableReader.java
│   └── LobConverter.java
├── loader/
│   ├── TestClassNameBasedDataSetLoader.java
│   ├── AnnotationResolver.java
│   ├── DataSetFactory.java
│   ├── DataSetMerger.java
│   └── DirectoryResolver.java
├── scenario/
│   ├── ScenarioFilter.java
│   ├── FilteredDataSet.java
│   └── FilteredTable.java
└── spi/
    ├── DefaultAssertionProvider.java
    ├── DefaultDataSetLoaderProvider.java
    ├── DefaultExpectationProvider.java
    ├── DefaultOperationProvider.java
    └── ScenarioNameResolverRegistry.java
```

---

## Architectural Patterns

### Layered Architecture

| Layer | Modules | Responsibility |
|-------|---------|----------------|
| Presentation | junit, spock, starters | Test framework integration |
| Application | api | Public interfaces and contracts |
| Domain | core (dataset, domain) | Business logic and entities |
| Infrastructure | core (jdbc, format) | Database and file system access |

### Domain-Driven Design Patterns

| Pattern | Implementation | Description |
|---------|----------------|-------------|
| Value Object | `TableName`, `ColumnName`, `CellValue` | Immutable objects with equality by value |
| Entity | `Table`, `Row` | Objects with identity within aggregate |
| Aggregate | `DataSet` | Root entity with consistency boundary |
| Factory | `DataSetFactory` | Complex object creation |
| Repository | `DataSourceRegistry` | Data source management |
| Domain Service | `DataSetComparator` | Stateless operations on entities |

### Service Provider Interface (SPI)

The framework uses SPI for loose coupling between modules:

```
┌─────────────────────┐     ServiceLoader     ┌─────────────────────────┐
│  db-tester-junit    │ ──────────────────────►  db-tester-core        │
│                     │                        │                         │
│  Uses:              │                        │  Provides:              │
│  - DataSetLoader    │                        │  - DataSetLoaderProvider│
│  - OperationProvider│                        │  - OperationProvider    │
│  - ScenarioResolver │                        │  - ScenarioResolver     │
└─────────────────────┘                        └─────────────────────────┘
```

### Strategy Pattern

Operations and comparison strategies use the strategy pattern:

| Strategy Interface | Implementations |
|-------------------|-----------------|
| `Operation` enum | NONE, INSERT, UPDATE, DELETE, REFRESH, CLEAN_INSERT, etc. |
| `ComparisonStrategy` | STRICT, IGNORE, NUMERIC, CASE_INSENSITIVE, TIMESTAMP_FLEXIBLE, NOT_NULL, REGEX |
| `TableMergeStrategy` | FIRST, LAST, UNION, UNION_ALL |
| `FormatProvider` | CsvFormatProvider, TsvFormatProvider |

---

## JPMS Support

### Full Module Support

The following modules provide complete `module-info.java`:

| Module | Module Name |
|--------|-------------|
| `db-tester-api` | `io.github.seijikohara.dbtester.api` |
| `db-tester-core` | `io.github.seijikohara.dbtester.core` |
| `db-tester-junit` | `io.github.seijikohara.dbtester.junit` |

### Automatic Module Names

The following modules use `Automatic-Module-Name` in `MANIFEST.MF`:

| Module | Automatic-Module-Name |
|--------|----------------------|
| `db-tester-spock` | `io.github.seijikohara.dbtester.spock` |
| `db-tester-junit-spring-boot-starter` | `io.github.seijikohara.dbtester.junit.spring.autoconfigure` |
| `db-tester-spock-spring-boot-starter` | `io.github.seijikohara.dbtester.spock.spring.autoconfigure` |

### Module Dependencies

Example `module-info.java` for the API module:

```java
module io.github.seijikohara.dbtester.api {
    requires java.sql;
    requires transitive org.jspecify;

    exports io.github.seijikohara.dbtester.api.annotation;
    exports io.github.seijikohara.dbtester.api.config;
    exports io.github.seijikohara.dbtester.api.context;
    exports io.github.seijikohara.dbtester.api.dataset;
    exports io.github.seijikohara.dbtester.api.domain;
    exports io.github.seijikohara.dbtester.api.exception;
    exports io.github.seijikohara.dbtester.api.loader;
    exports io.github.seijikohara.dbtester.api.operation;
    exports io.github.seijikohara.dbtester.api.scenario;
    exports io.github.seijikohara.dbtester.api.spi;

    uses io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider;
    uses io.github.seijikohara.dbtester.api.spi.OperationProvider;
    uses io.github.seijikohara.dbtester.api.spi.AssertionProvider;
    uses io.github.seijikohara.dbtester.api.spi.ExpectationProvider;
    uses io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver;
}
```

---

## Related Specifications

- [Overview](01-OVERVIEW.md) - High-level framework description
- [Public API](03-PUBLIC-API.md) - Detailed API documentation
- [SPI](08-SPI.md) - Service Provider Interface details
