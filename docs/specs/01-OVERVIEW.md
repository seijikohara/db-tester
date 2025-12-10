# DB Tester Specification - Overview

This document provides a high-level overview of the DB Tester framework.

## Purpose

DB Tester is a database testing framework for JUnit and Spock Framework. The framework provides annotation-driven data preparation and state verification using CSV/TSV-based test data files.

The framework addresses the following challenges in database testing:

| Challenge | Solution |
|-----------|----------|
| Test data management | File-based datasets in structured directories |
| Repetitive setup code | Declarative `@Preparation` and `@Expectation` annotations |
| Multi-database testing | Named `DataSource` registry with explicit binding |
| Test isolation | Automatic cleanup via `CLEAN_INSERT` operation |
| Data format flexibility | Support for CSV and TSV formats |

## Key Concepts

### Preparation Phase

The preparation phase executes before each test method. The framework:

1. Resolves dataset files based on test class and method names
2. Filters rows by scenario markers when applicable
3. Applies the configured database operation (default: `CLEAN_INSERT`)

### Expectation Phase

The expectation phase executes after each test method. The framework:

1. Loads expected datasets from the designated directory (default: `expected/` subdirectory)
2. Reads actual data from the database
3. Compares expected and actual states using configurable comparison strategies

### Convention-Based Discovery

The framework resolves dataset locations automatically:

```
src/test/resources/
└── {package}/{TestClassName}/
    ├── TABLE_NAME.csv           # Preparation data
    └── expected/
        └── TABLE_NAME.csv       # Expectation data
```

### Scenario Filtering

Multiple test methods can share dataset files using scenario markers:

| [Scenario] | id | name |
|------------|----|------|
| testCreate | 1  | Alice |
| testUpdate | 2  | Bob |

The framework filters rows based on the current test method name.

## Design Philosophy

### Convention over Configuration

The framework minimizes explicit configuration by establishing sensible defaults:

- Dataset location derived from test class package and name
- Expectation suffix defaults to `/expected`
- Scenario marker column defaults to `[Scenario]`
- Data format defaults to CSV

### Separation of API and Implementation

The framework separates public API from internal implementation:

| Layer | Visibility | Purpose |
|-------|------------|---------|
| `db-tester-api` | Public | Annotations, configuration, SPI interfaces |
| `db-tester-core` | Internal | JDBC operations, format parsing, SPI implementations |

Test framework modules (`db-tester-junit`, `db-tester-spock`) depend only on the API module at compile time. The core module is loaded at runtime via Java ServiceLoader.

### Immutability

All public API classes are immutable:

- Configuration records use Java `record` types
- Value objects (TableName, ColumnName, CellValue) are final and immutable
- Returned collections are unmodifiable

### Null Safety

The framework uses JSpecify annotations for null safety:

- All packages declare `@NullMarked` in `package-info.java`
- Nullable parameters and return types use `@Nullable` annotation
- NullAway enforces null safety at compile time

## Technology Requirements

| Component | Version | Notes |
|-----------|---------|-------|
| Java | 21 or later | JPMS module-info.java support |
| Groovy | 5 or later | For Spock modules |
| JUnit | 6 or later | JUnit Jupiter extension model |
| Spock Framework | 2 or later | Annotation-driven extension model |
| Spring Boot | 4 or later | For Spring Boot Starter modules |

### Database Compatibility

The framework uses standard JDBC operations and supports any JDBC-compliant database:

- H2
- MySQL
- PostgreSQL
- Derby
- HSQLDB
- MS SQL Server
- Oracle

## Module Summary

| Module | Description | Documentation |
|--------|-------------|---------------|
| `db-tester-api` | Public API module | [Architecture](02-ARCHITECTURE) |
| `db-tester-core` | Internal implementation | [Architecture](02-ARCHITECTURE) |
| `db-tester-junit` | JUnit Jupiter extension | [Test Frameworks](07-TEST-FRAMEWORKS) |
| `db-tester-spock` | Spock Framework extension | [Test Frameworks](07-TEST-FRAMEWORKS) |
| `db-tester-junit-spring-boot-starter` | Spring Boot integration for JUnit | [Test Frameworks](07-TEST-FRAMEWORKS) |
| `db-tester-spock-spring-boot-starter` | Spring Boot integration for Spock | [Test Frameworks](07-TEST-FRAMEWORKS) |
| `db-tester-bom` | Bill of Materials for dependency management | - |

## Related Specifications

- [Architecture](02-ARCHITECTURE) - Module structure and dependencies
- [Public API](03-PUBLIC-API) - Annotations and configuration classes
- [Configuration](04-CONFIGURATION) - Configuration options and conventions
- [Data Formats](05-DATA-FORMATS) - CSV/TSV file structure and parsing
- [Database Operations](06-DATABASE-OPERATIONS) - Supported CRUD operations
- [Test Frameworks](07-TEST-FRAMEWORKS) - JUnit and Spock integration
- [SPI](08-SPI) - Service Provider Interface extension points
- [Error Handling](09-ERROR-HANDLING) - Error messages and exception types
