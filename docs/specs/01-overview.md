# DB Tester Specification - Overview

This document provides a high-level overview of the DB Tester framework.

## Purpose

DB Tester is a database testing framework for JUnit, Spock, and Kotest. The framework provides annotation-driven data preparation and state verification using CSV or TSV test data files.

The framework addresses the following challenges in database testing:

| Challenge | Solution |
|-----------|----------|
| Test data management | File-based datasets in structured directories |
| Repetitive setup code | Declarative `@DataSet` and `@ExpectedDataSet` annotations |
| Multi-database testing | Named `DataSource` registry with explicit binding |
| Test isolation | Automatic cleanup via configurable database operations |
| Data format flexibility | Support for CSV and TSV formats |
| Validation flexibility | Column-level comparison strategies |

## Key Concepts

### DataSet Phase

The data set phase executes before each test method. The framework performs the following steps:

1. Resolves dataset files based on test class and method names
2. Filters rows by scenario markers when applicable
3. Orders tables using the configured strategy (default: `AUTO`)
4. Applies the configured database operation (default: `CLEAN_INSERT`)

Available operations: `NONE`, `INSERT`, `UPDATE`, `DELETE`, `DELETE_ALL`, `REFRESH`, `TRUNCATE_TABLE`, `CLEAN_INSERT`, `TRUNCATE_INSERT`.

### ExpectedDataSet Phase

The expected data set phase executes after each test method. The framework performs the following steps:

1. Loads expected datasets from the designated directory (default: `expected/` subdirectory)
2. Reads actual data from the database
3. Compares expected and actual states using configurable comparison strategies
4. Reports differences with structured error messages

Available comparison strategies: `STRICT`, `IGNORE`, `NUMERIC`, `CASE_INSENSITIVE`, `TIMESTAMP_FLEXIBLE`, `NOT_NULL`, `REGEX`.

### Convention-Based Discovery

The framework resolves dataset locations automatically based on test class package and name:

```
src/test/resources/
└── {package}/{TestClassName}/
    ├── TABLE_NAME.csv           # DataSet data
    ├── load-order.txt           # Table ordering (optional)
    └── expected/
        └── TABLE_NAME.csv       # ExpectedDataSet data
```

### Scenario Filtering

Multiple test methods share dataset files using scenario markers in a `[Scenario]` column:

| [Scenario] | id | name |
|------------|----|------|
| testCreate | 1  | Alice |
| testUpdate | 2  | Bob |

The framework filters rows based on the current test method name.

## Design Philosophy

### Convention over Configuration

The framework minimizes explicit configuration by establishing sensible defaults:

- Dataset location derived from test class package and name
- ExpectedDataSet suffix defaults to `/expected`
- Scenario marker column defaults to `[Scenario]`
- Data format defaults to CSV
- Table ordering strategy defaults to `AUTO`
- DataSet operation defaults to `CLEAN_INSERT`

### Separation of API and Implementation

The framework separates public API from internal implementation:

| Layer | Visibility | Purpose |
|-------|------------|---------|
| `db-tester-api` | Public | Annotations, configuration, domain models, SPI interfaces |
| `db-tester-core` | Internal | JDBC operations, format parsing, SPI implementations |

Test framework modules depend only on the API module at compile time. The core module loads at runtime via Java ServiceLoader.

### Immutability

All public API classes are immutable:

- Configuration uses Java `record` types
- Value objects (`TableName`, `ColumnName`, `CellValue`) are final and immutable
- Returned collections are unmodifiable copies

### Null Safety

The framework uses JSpecify annotations for null safety:

- All packages declare `@NullMarked` in `package-info.java`
- Nullable parameters and return types use `@Nullable` annotation
- NullAway enforces null safety at compile time

## Technology Requirements

| Component | Version | Notes |
|-----------|---------|-------|
| Java | 21 or later | JPMS module support |
| Groovy | 5 or later | For Spock modules |
| Kotlin | 2 or later | For Kotest modules |
| JUnit | 6 or later | JUnit Jupiter extension model |
| Spock | 2 or later | Annotation-driven extension model |
| Kotest | 6 or later | AnnotationSpec with TestCaseExtension |
| Spring Boot | 4 or later | For Spring Boot Starter modules |

### Database Compatibility

The framework uses standard JDBC operations. It supports any JDBC-compliant database, including:

- H2
- HSQLDB
- Derby
- PostgreSQL
- MySQL
- MS SQL Server
- Oracle

## Module Summary

| Module | Description | Documentation |
|--------|-------------|---------------|
| `db-tester-api` | Public API module | [Architecture](02-architecture) |
| `db-tester-core` | Internal implementation | [Architecture](02-architecture) |
| `db-tester-junit` | JUnit Jupiter extension | [Test Frameworks](07-test-frameworks) |
| `db-tester-spock` | Spock extension | [Test Frameworks](07-test-frameworks) |
| `db-tester-kotest` | Kotest AnnotationSpec extension | [Test Frameworks](07-test-frameworks) |
| `db-tester-junit-spring-boot-starter` | Spring Boot integration for JUnit | [Test Frameworks](07-test-frameworks) |
| `db-tester-spock-spring-boot-starter` | Spring Boot integration for Spock | [Test Frameworks](07-test-frameworks) |
| `db-tester-kotest-spring-boot-starter` | Spring Boot integration for Kotest | [Test Frameworks](07-test-frameworks) |
| `db-tester-bom` | Bill of Materials for dependency management | - |

## Related Specifications

- [Architecture](02-architecture) - Module structure and dependencies
- [Public API](03-public-api) - Annotations and configuration classes
- [Configuration](04-configuration) - Configuration options and conventions
- [Data Formats](05-data-formats) - CSV and TSV file structure and parsing
- [Database Operations](06-database-operations) - Supported CRUD operations
- [Test Frameworks](07-test-frameworks) - JUnit, Spock, and Kotest integration
- [SPI](08-spi) - Service Provider Interface extension points
- [Error Handling](09-error-handling) - Error messages and exception types
